from __future__ import annotations

import argparse
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import html
import json
import struct
from urllib.parse import parse_qs, unquote, urlparse
import zlib

from .catalog import BOOKS, MAX_SEARCH_LIMIT, book_to_api, category_list, get_book, search_books


class BookServerHandler(BaseHTTPRequestHandler):
    server_version = "BookShelfLocalServer/0.1"

    def do_GET(self) -> None:
        parsed = urlparse(self.path)
        path = parsed.path.rstrip("/") or "/"
        query = parse_qs(parsed.query)
        base_url = f"http://{self.headers.get('Host', '127.0.0.1:8000')}"

        if path == "/health":
            self.send_json({"status": "ok", "books": len(BOOKS)})
            return

        if path == "/api/books":
            raw_query = query.get("q", ["book"])[0]
            limit = parse_limit(query.get("limit", ["20"])[0])
            items = [book_to_api(book, base_url) for book in search_books(raw_query, limit)]
            self.send_json({"items": items, "categories": category_list()})
            return

        if path.startswith("/api/books/") and path.endswith("/cover.png"):
            book_id = unquote(path.removeprefix("/api/books/").removesuffix("/cover.png").strip("/"))
            book = get_book(book_id)
            if book is None:
                self.send_not_found()
                return
            self.send_bytes(make_cover_png(book.color), "image/png")
            return

        if path.startswith("/api/books/") and path.endswith("/content"):
            book_id = unquote(path.removeprefix("/api/books/").removesuffix("/content").strip("/"))
            book = get_book(book_id)
            if book is None:
                self.send_not_found()
                return
            self.send_json(
                {
                    "book": book_to_api(book, base_url),
                    "chapters": list(book.chapters),
                }
            )
            return

        if path.startswith("/api/books/"):
            book_id = unquote(path.removeprefix("/api/books/").strip("/"))
            book = get_book(book_id)
            if book is None:
                self.send_not_found()
                return
            self.send_json({"book": book_to_api(book, base_url)})
            return

        if path.startswith("/books/") and path.endswith("/read"):
            book_id = unquote(path.removeprefix("/books/").removesuffix("/read").strip("/"))
            book = get_book(book_id)
            if book is None:
                self.send_not_found()
                return
            self.send_html(render_reader_html(book_id, base_url))
            return

        self.send_not_found()

    def log_message(self, format: str, *args: object) -> None:
        print(f"{self.address_string()} - {format % args}")

    def send_json(self, payload: dict[str, object], status: HTTPStatus = HTTPStatus.OK) -> None:
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(data)

    def send_html(self, body: str) -> None:
        data = body.encode("utf-8")
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def send_bytes(self, data: bytes, content_type: str) -> None:
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def send_not_found(self) -> None:
        self.send_json({"error": "Not found"}, status=HTTPStatus.NOT_FOUND)


def parse_limit(value: str) -> int:
    try:
        return max(1, min(int(value), MAX_SEARCH_LIMIT))
    except ValueError:
        return 20


def render_reader_html(book_id: str, base_url: str) -> str:
    book = get_book(book_id)
    if book is None:
        return "<!doctype html><title>Not found</title><h1>Not found</h1>"

    chapters = "\n".join(
        f"<section><h2>Part {index}</h2><p>{html.escape(chapter)}</p></section>"
        for index, chapter in enumerate(book.chapters, start=1)
    )
    authors = ", ".join(book.authors)
    categories = ", ".join(book.categories)
    return f"""<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>{html.escape(book.title)}</title>
  <style>
    body {{
      margin: 0;
      padding: 28px 18px 44px;
      font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
      line-height: 1.65;
      color: #202124;
      background: #faf8f4;
    }}
    main {{
      max-width: 760px;
      margin: 0 auto;
    }}
    img {{
      width: 116px;
      height: 174px;
      border-radius: 8px;
      object-fit: cover;
      float: right;
      margin: 0 0 16px 18px;
      box-shadow: 0 8px 22px rgba(0, 0, 0, 0.22);
    }}
    h1 {{
      margin: 0 0 6px;
      font-size: 30px;
      line-height: 1.15;
    }}
    .meta {{
      color: #616161;
      margin-bottom: 20px;
    }}
    section {{
      clear: both;
      padding-top: 12px;
    }}
    h2 {{
      font-size: 20px;
      margin: 24px 0 8px;
    }}
  </style>
</head>
<body>
  <main>
    <img src="{base_url}/api/books/{html.escape(book.id)}/cover.png" alt="">
    <h1>{html.escape(book.title)}</h1>
    <div class="meta">{html.escape(authors)} - {html.escape(categories)}</div>
    <p>{html.escape(book.description)}</p>
    {chapters}
  </main>
</body>
</html>"""


def make_cover_png(color: tuple[int, int, int], width: int = 320, height: int = 480) -> bytes:
    background = tuple(max(0, min(channel, 255)) for channel in color)
    accent = tuple(min(channel + 62, 255) for channel in background)
    paper = (238, 232, 220)
    rows = []
    for y in range(height):
        row = bytearray()
        for x in range(width):
            pixel = background
            if 34 < x < width - 34 and 58 < y < height - 58:
                pixel = paper
            if 54 < x < width - 54 and y in range(96, 105):
                pixel = accent
            if 54 < x < width - 54 and y in range(136, 143):
                pixel = background
            if 54 < x < width - 54 and y in range(166, 172):
                pixel = background
            if 54 < x < width - 90 and y in range(222, 228):
                pixel = accent
            if 54 < x < width - 78 and y in range(252, 258):
                pixel = accent
            row.extend(pixel)
        rows.append(b"\x00" + bytes(row))

    raw_data = b"".join(rows)
    return (
        b"\x89PNG\r\n\x1a\n"
        + png_chunk(b"IHDR", struct.pack("!IIBBBBB", width, height, 8, 2, 0, 0, 0))
        + png_chunk(b"IDAT", zlib.compress(raw_data, level=9))
        + png_chunk(b"IEND", b"")
    )


def png_chunk(chunk_type: bytes, data: bytes) -> bytes:
    return (
        struct.pack("!I", len(data))
        + chunk_type
        + data
        + struct.pack("!I", zlib.crc32(chunk_type + data) & 0xFFFFFFFF)
    )


def run(host: str, port: int) -> None:
    server = ThreadingHTTPServer((host, port), BookServerHandler)
    print(f"BookShelf local server: http://{host}:{port}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping server")
    finally:
        server.server_close()


def main() -> None:
    parser = argparse.ArgumentParser(description="Run the BookShelf local book server")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", default=8000, type=int)
    args = parser.parse_args()
    run(args.host, args.port)


if __name__ == "__main__":
    main()
