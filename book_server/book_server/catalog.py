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


@dataclass(frozen=True)
class GenreSeed:
    key: str
    display: str
    topics: tuple[str, ...]
    color: tuple[int, int, int]
    title_word: str
    description_focus: str


FEATURED_BOOKS: tuple[BookRecord, ...] = (
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


GENRE_SEEDS: tuple[GenreSeed, ...] = (
    GenreSeed(
        key="fantasy",
        display="Fantasy",
        topics=("Magic", "Epic Quest", "Mythic Worlds", "Dragons"),
        color=(86, 80, 143),
        title_word="Realm",
        description_focus="magic systems, unusual kingdoms, and personal courage",
    ),
    GenreSeed(
        key="science-fiction",
        display="Science Fiction",
        topics=("Space", "Future Society", "Robotics", "Time Travel"),
        color=(60, 103, 155),
        title_word="Signal",
        description_focus="technology, space travel, and the social cost of invention",
    ),
    GenreSeed(
        key="detective",
        display="Detective",
        topics=("Investigation", "Crime", "Evidence", "Private Detective"),
        color=(67, 84, 96),
        title_word="Case",
        description_focus="clues, witnesses, motives, and careful reconstruction of events",
    ),
    GenreSeed(
        key="romance",
        display="Romance",
        topics=("Relationships", "Family", "Letters", "Modern Love"),
        color=(151, 74, 103),
        title_word="Letter",
        description_focus="relationships, trust, family expectations, and emotional choice",
    ),
    GenreSeed(
        key="classic-literature",
        display="Classic Literature",
        topics=("Classics", "Social Fiction", "Moral Choice", "Literary Fiction"),
        color=(117, 88, 61),
        title_word="Portrait",
        description_focus="society, language, conflict, and long-term moral consequences",
    ),
    GenreSeed(
        key="horror",
        display="Horror",
        topics=("Supernatural", "Suspense", "Gothic", "Fear"),
        color=(96, 55, 71),
        title_word="Shadow",
        description_focus="fear, isolation, disturbing discoveries, and hidden threats",
    ),
    GenreSeed(
        key="adventure",
        display="Adventure",
        topics=("Travel", "Expedition", "Survival", "Sea Stories"),
        color=(48, 111, 92),
        title_word="Expedition",
        description_focus="travel, risk, survival, and decisions under pressure",
    ),
    GenreSeed(
        key="psychology",
        display="Psychology",
        topics=("Self Development", "Habits", "Communication", "Motivation"),
        color=(97, 103, 79),
        title_word="Mind",
        description_focus="habits, motivation, memory, communication, and behavior patterns",
    ),
    GenreSeed(
        key="business",
        display="Business",
        topics=("Management", "Startup", "Marketing", "Finance"),
        color=(54, 113, 124),
        title_word="Strategy",
        description_focus="teams, markets, planning, finance, and practical decision making",
    ),
    GenreSeed(
        key="history",
        display="History",
        topics=("World History", "Culture", "War", "Civilization"),
        color=(128, 83, 55),
        title_word="Chronicle",
        description_focus="events, culture, institutions, and how historical change happens",
    ),
    GenreSeed(
        key="biography",
        display="Biography",
        topics=("Memoir", "Leadership", "Artists", "Scientists"),
        color=(96, 91, 129),
        title_word="Life",
        description_focus="personal growth, professional choices, failures, and achievements",
    ),
    GenreSeed(
        key="programming",
        display="Programming",
        topics=("Kotlin", "Android", "Software Design", "Algorithms"),
        color=(58, 93, 133),
        title_word="Code",
        description_focus="software design, debugging, APIs, data, and maintainable code",
    ),
    GenreSeed(
        key="children",
        display="Children",
        topics=("Children's Literature", "School", "Friendship", "Imagination"),
        color=(150, 111, 62),
        title_word="Story",
        description_focus="friendship, imagination, school life, and simple moral choices",
    ),
    GenreSeed(
        key="comics",
        display="Comics",
        topics=("Graphic Novel", "Superheroes", "Humor", "Visual Storytelling"),
        color=(62, 114, 151),
        title_word="Panel",
        description_focus="visual storytelling, expressive scenes, conflict, and humor",
    ),
)

BOOKS_PER_GENRE = 9
MAIN_GENRE_LABELS = tuple(seed.display for seed in GENRE_SEEDS)
MAX_SEARCH_LIMIT = 150


def generate_genre_books(seed: GenreSeed, seed_index: int) -> tuple[BookRecord, ...]:
    return tuple(
        BookRecord(
            id=f"{seed.key}-demo-{number:02d}",
            title=f"{seed.display} {seed.title_word} {number:02d}",
            authors=(f"{seed.display} Demo Author {number:02d}",),
            description=(
                f"A test {seed.display.lower()} book about {seed.description_focus}. "
                f"It is generated for demo search, recommendations, and in-app reading scenarios."
            ),
            categories=(
                seed.display,
                seed.topics[(number - 1) % len(seed.topics)],
                seed.topics[number % len(seed.topics)],
            ),
            published_date=str(2001 + ((seed_index * BOOKS_PER_GENRE + number) % 24)),
            page_count=120 + ((seed_index * 29 + number * 17) % 420),
            language="en",
            color=shift_color(seed.color, number),
            chapters=(
                f"Part 1. This {seed.display.lower()} demo book introduces the main theme: "
                f"{seed.description_focus}.",
                f"Part 2. The conflict develops through {seed.topics[(number - 1) % len(seed.topics)].lower()} "
                f"and {seed.topics[number % len(seed.topics)].lower()}.",
                f"Part 3. The ending gives enough material to test reading progress, statistics, and genre metadata.",
            ),
        )
        for number in range(1, BOOKS_PER_GENRE + 1)
    )


def shift_color(color: tuple[int, int, int], offset: int) -> tuple[int, int, int]:
    return tuple(min(220, max(35, channel + (offset % 5) * 9 - 18)) for channel in color)


BOOKS: tuple[BookRecord, ...] = FEATURED_BOOKS + tuple(
    book
    for seed_index, seed in enumerate(GENRE_SEEDS)
    for book in generate_genre_books(seed, seed_index)
)


def search_books(query: str, limit: int) -> list[BookRecord]:
    normalized_query = normalize(query)
    safe_limit = max(1, min(limit, MAX_SEARCH_LIMIT))
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
