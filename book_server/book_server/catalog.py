from __future__ import annotations

from dataclasses import dataclass
import re
from typing import Iterable


@dataclass(frozen=True)
class BookRecord:
    id: str
    title: str
    authors: tuple[str, ...]
    description: str
    categories: tuple[str, ...]
    published_date: str
    page_count: int
    language: str
    color: tuple[int, int, int]
    chapters: tuple[str, ...]


BOOKS: tuple[BookRecord, ...] = (
    BookRecord(
        id="war-and-peace-demo",
        title="War and Peace",
        authors=("Leo Tolstoy",),
        description="A demo record for a large historical novel about families, war, society, and personal choice.",
        categories=("Classics", "Historical Fiction", "War", "Russian Literature"),
        published_date="1869",
        page_count=1225,
        language="en",
        color=(121, 78, 55),
        chapters=(
            "Chapter 1. The evening begins with conversation about war, politics, and duty. "
            "The reader sees a society where private life and history are already tied together.",
            "Chapter 2. Families prepare for change. Personal hopes, fear of uncertainty, and the pressure "
            "of public events shape each decision.",
            "Chapter 3. The army road makes the conflict concrete. The story turns from salon conversation "
            "to movement, discipline, and the cost of ambition.",
        ),
    ),
    BookRecord(
        id="alice-demo",
        title="Alice's Adventures in Wonderland",
        authors=("Lewis Carroll",),
        description="A fantasy journey through a strange world built on wordplay, curiosity, and impossible rules.",
        categories=("Fantasy", "Adventure", "Children's Literature"),
        published_date="1865",
        page_count=192,
        language="en",
        color=(68, 116, 153),
        chapters=(
            "Chapter 1. Alice follows a hurried white rabbit and discovers that ordinary rules no longer work.",
            "Chapter 2. Changes in size make every room, door, and object part of the puzzle.",
            "Chapter 3. The journey continues as conversation becomes a game and every answer creates a new question.",
        ),
    ),
    BookRecord(
        id="sherlock-demo",
        title="The Adventures of Sherlock Holmes",
        authors=("Arthur Conan Doyle",),
        description="A collection of detective cases focused on observation, deduction, and hidden motives.",
        categories=("Detective", "Mystery", "Classics"),
        published_date="1892",
        page_count=307,
        language="en",
        color=(55, 86, 70),
        chapters=(
            "Case 1. Holmes treats small details as evidence and turns a minor inconsistency into the key.",
            "Case 2. A client brings a story that seems impossible until the timeline is rebuilt step by step.",
            "Case 3. The solution depends less on force than on attention, patience, and precise questions.",
        ),
    ),
    BookRecord(
        id="pride-demo",
        title="Pride and Prejudice",
        authors=("Jane Austen",),
        description="A social novel about family, reputation, judgment, and gradual self-knowledge.",
        categories=("Romance", "Classics", "Social Fiction"),
        published_date="1813",
        page_count=432,
        language="en",
        color=(136, 73, 112),
        chapters=(
            "Chapter 1. A new neighbor changes the expectations of several families at once.",
            "Chapter 2. First impressions become social facts before anyone has enough information.",
            "Chapter 3. Conversation reveals pride, caution, and the limits of quick judgment.",
        ),
    ),
    BookRecord(
        id="moby-dick-demo",
        title="Moby-Dick",
        authors=("Herman Melville",),
        description="A sea novel about obsession, labor, myth, and the dangerous scale of one captain's goal.",
        categories=("Adventure", "Sea Stories", "Classics"),
        published_date="1851",
        page_count=635,
        language="en",
        color=(42, 92, 118),
        chapters=(
            "Chapter 1. The narrator chooses the sea as a way to leave ordinary life and test himself.",
            "Chapter 2. The ship becomes a small world with its own hierarchy, language, and risks.",
            "Chapter 3. The hunt grows into a symbol of obsession that affects every person on board.",
        ),
    ),
    BookRecord(
        id="time-machine-demo",
        title="The Time Machine",
        authors=("H. G. Wells",),
        description="A science fiction story about travel through time and the consequences of social division.",
        categories=("Science Fiction", "Adventure", "Classics"),
        published_date="1895",
        page_count=118,
        language="en",
        color=(88, 95, 148),
        chapters=(
            "Chapter 1. The inventor explains a machine that treats time as a dimension for movement.",
            "Chapter 2. The future first looks peaceful, but the surface hides a different structure.",
            "Chapter 3. Discovery turns into danger as the traveler understands the cost of that future.",
        ),
    ),
)


def search_books(query: str, limit: int) -> list[BookRecord]:
    normalized_query = normalize(query)
    safe_limit = max(1, min(limit, 100))
    if not normalized_query or normalized_query == "book":
        return list(BOOKS[:safe_limit])

    terms = normalized_query.split()
    ranked: list[tuple[int, BookRecord]] = []
    for book in BOOKS:
        haystack = normalize(
            " ".join(
                (
                    book.title,
                    " ".join(book.authors),
                    book.description,
                    " ".join(book.categories),
                )
            )
        )
        score = sum(1 for term in terms if term in haystack)
        if score > 0:
            ranked.append((score, book))

    ranked.sort(key=lambda item: (-item[0], item[1].title))
    return [book for _, book in ranked[:safe_limit]]


def get_book(book_id: str) -> BookRecord | None:
    return next((book for book in BOOKS if book.id == book_id), None)


def book_to_api(book: BookRecord, base_url: str) -> dict[str, object]:
    return {
        "id": book.id,
        "title": book.title,
        "authors": list(book.authors),
        "description": book.description,
        "categories": list(book.categories),
        "publishedDate": book.published_date,
        "pageCount": book.page_count,
        "language": book.language,
        "coverUrl": f"{base_url}/api/books/{book.id}/cover.png",
        "readerUrl": f"{base_url}/books/{book.id}/read",
    }


def normalize(value: str) -> str:
    return re.sub(r"[^a-z0-9а-яё]+", " ", value.lower()).strip()


def category_list(books: Iterable[BookRecord] = BOOKS) -> list[str]:
    categories = {category for book in books for category in book.categories}
    return sorted(categories)
