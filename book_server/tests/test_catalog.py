import unittest
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from book_server.catalog import book_to_api, category_list, get_book, search_books
from book_server.main import make_cover_png, parse_limit


class CatalogTest(unittest.TestCase):
    def test_search_matches_title_author_and_genre(self):
        self.assertEqual(search_books("tolstoy", 10)[0].id, "war-and-peace-demo")
        self.assertEqual(search_books("detective", 10)[0].id, "sherlock-demo")
        self.assertEqual(search_books("science fiction", 10)[0].id, "time-machine-demo")

    def test_search_limits_results(self):
        self.assertEqual(len(search_books("book", 2)), 2)

    def test_book_to_api_contains_reader_and_cover_urls(self):
        book = get_book("alice-demo")
        payload = book_to_api(book, "http://127.0.0.1:8000")

        self.assertEqual(payload["id"], "alice-demo")
        self.assertEqual(payload["readerUrl"], "http://127.0.0.1:8000/books/alice-demo/read")
        self.assertEqual(payload["coverUrl"], "http://127.0.0.1:8000/api/books/alice-demo/cover.png")

    def test_categories_are_unique_and_sorted(self):
        categories = category_list()

        self.assertEqual(categories, sorted(set(categories)))
        self.assertIn("Fantasy", categories)

    def test_parse_limit_keeps_safe_bounds(self):
        self.assertEqual(parse_limit("bad"), 20)
        self.assertEqual(parse_limit("-1"), 1)
        self.assertEqual(parse_limit("500"), 100)

    def test_cover_endpoint_returns_png_bytes(self):
        png = make_cover_png((1, 2, 3), width=8, height=8)

        self.assertTrue(png.startswith(b"\x89PNG\r\n\x1a\n"))


if __name__ == "__main__":
    unittest.main()
