import unittest
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from book_server.catalog import BOOKS, MAIN_GENRE_LABELS, book_to_api, category_list, get_book, search_books
from book_server.main import make_cover_png, parse_limit


class CatalogTest(unittest.TestCase):
    def test_catalog_has_demo_books_for_defense_scenarios(self):
        self.assertGreaterEqual(len(BOOKS), 100)
        self.assertLessEqual(len(BOOKS), 150)

    def test_catalog_covers_android_main_genres(self):
        categories = set(category_list())

        for genre in MAIN_GENRE_LABELS:
            self.assertIn(genre, categories)

    def test_search_matches_title_author_and_genre(self):
        self.assertIn("war-and-peace-demo", [book.id for book in search_books("tolstoy", 10)])
        self.assertIn("sherlock-demo", [book.id for book in search_books("detective", 20)])
        self.assertIn("time-machine-demo", [book.id for book in search_books("science fiction", 20)])

    def test_search_limits_results(self):
        self.assertEqual(len(search_books("book", 2)), 2)

    def test_search_returns_books_for_android_genre_queries(self):
        queries = (
            "fantasy",
            "science fiction",
            "detective",
            "romance",
            "classic literature",
            "horror",
            "adventure",
            "psychology",
            "business",
            "history",
            "biography",
            "programming",
            "children",
            "comics",
        )

        for query in queries:
            with self.subTest(query=query):
                self.assertGreaterEqual(len(search_books(query, 20)), 9)

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
        self.assertEqual(parse_limit("500"), 150)

    def test_cover_endpoint_returns_png_bytes(self):
        png = make_cover_png((1, 2, 3), width=8, height=8)

        self.assertTrue(png.startswith(b"\x89PNG\r\n\x1a\n"))


if __name__ == "__main__":
    unittest.main()
