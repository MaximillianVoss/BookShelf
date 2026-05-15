# BookShelf SQLite Schema

## ASCII ER schema

```text
┌────────────────────┐
│ users              │
│--------------------│
│ PK user_id         │
│ username           │
│ email              │
│ password_hash      │
│ password_salt      │
│ created_at         │
│ last_login_at      │
└─────────┬──────────┘
          │ 1
          │
          │ N
┌─────────▼──────────┐        N          1        ┌────────────────────┐
│ user_books         │────────────────────────────►│ books              │
│--------------------│                             │--------------------│
│ PK user_book_id    │                             │ PK book_id         │
│ FK user_id         │                             │ FK source_id       │
│ FK book_id         │                             │ external_id        │
│ status             │                             │ title              │
│ rating             │                             │ subtitle           │
│ review             │                             │ description        │
│ added_at           │                             │ published_date     │
│ started_at         │                             │ page_count         │
│ finished_at        │                             │ isbn_10 / isbn_13  │
│ updated_at         │                             │ thumbnail_url      │
└─────────┬──────────┘                             │ preview_link       │
          │ 1                                      └─────────┬──────────┘
          │                                                  │ N
          │ N                                                │
┌─────────▼──────────┐                             ┌─────────▼──────────┐
│ reading_sessions   │                             │ book_sources       │
│--------------------│                             │--------------------│
│ PK session_id      │                             │ PK source_id       │
│ FK user_book_id    │                             │ code               │
│ read_date          │                             │ name               │
│ minutes_read       │                             │ base_url           │
│ pages_read         │                             └────────────────────┘
│ note               │
└────────────────────┘


┌────────────────────┐       ┌────────────────────┐       ┌────────────────────┐
│ books              │  1  N │ book_authors       │ N  1  │ authors            │
│--------------------│◄──────│--------------------│──────►│--------------------│
│ PK book_id         │       │ PK/FK book_id      │       │ PK author_id       │
└────────────────────┘       │ PK/FK author_id    │       │ name               │
                             │ author_order       │       └────────────────────┘
                             └────────────────────┘


┌────────────────────┐       ┌────────────────────┐       ┌────────────────────┐
│ books              │  1  N │ book_genres        │ N  1  │ genres             │
│--------------------│◄──────│--------------------│──────►│--------------------│
│ PK book_id         │       │ PK/FK book_id      │       │ PK genre_id        │
└────────────────────┘       │ PK/FK genre_id     │       │ name               │
                             └────────────────────┘       └────────────────────┘


┌────────────────────┐        N          1        ┌────────────────────┐
│ book_recommendations│───────────────────────────►│ books              │
│--------------------│                             │--------------------│
│ PK recommendation_id│                             │ PK book_id         │
│ FK user_id          │                             └────────────────────┘
│ FK book_id          │
│ reason              │        N          1        ┌────────────────────┐
│ score               │───────────────────────────►│ users              │
│ created_at          │                             │--------------------│
└────────────────────┘                             │ PK user_id         │
                                                   └────────────────────┘
```

## Table roles

`users` stores registered local users. Passwords must not be stored as plain text; keep only hash and salt.

`books` stores book data from Open Library, HTML-parsed book pages, or manual input. `source_id + external_id` prevents duplicate external books.

`authors`, `book_authors`, `genres`, and `book_genres` normalize many-to-many fields from API responses.

`user_books` is the user's personal library. Status values: `WANT_TO_READ`, `READING`, `READ`, `PAUSED`, `DROPPED`.

`reading_sessions` stores reading activity by date. It supports a reading/non-reading calendar, total reading time, and pages read charts.

`book_recommendations` can cache generated recommendations. The recommendation algorithm can also calculate results dynamically from `v_user_genre_stats`.

## Useful views

`v_user_library` is convenient for the personal cabinet: list, filtering by status, rating, and book metadata.

`v_reading_calendar` groups reading activity by date for calendar and chart screens.

`v_user_genre_stats` counts genres among read books and can be used for recommendation selection.

## Recommendation logic

```text
1. Select the user's read books from user_books where status = 'READ'.
2. Join them with book_genres and genres.
3. Find the most frequent genres in v_user_genre_stats.
4. Search external sources by top genres, for example Open Library queries or HTML search pages.
5. Exclude books already present in the user's library.
6. Save candidates to book_recommendations with reason and score.
```
