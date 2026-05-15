PRAGMA foreign_keys = ON;

-- SQLite schema for BookShelf local storage.
-- Recommended Android implementation: Room over SQLite.

CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT UNIQUE,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    last_login_at TEXT
);

CREATE TABLE IF NOT EXISTS book_sources (
    source_id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    base_url TEXT
);

CREATE TABLE IF NOT EXISTS books (
    book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    source_id INTEGER,
    external_id TEXT,
    title TEXT NOT NULL,
    subtitle TEXT,
    description TEXT,
    published_date TEXT,
    page_count INTEGER CHECK (page_count IS NULL OR page_count >= 0),
    language TEXT,
    isbn_10 TEXT,
    isbn_13 TEXT,
    thumbnail_url TEXT,
    preview_link TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (source_id) REFERENCES book_sources(source_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS authors (
    author_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS book_authors (
    book_id INTEGER NOT NULL,
    author_id INTEGER NOT NULL,
    author_order INTEGER NOT NULL DEFAULT 0 CHECK (author_order >= 0),
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(author_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS book_genres (
    book_id INTEGER NOT NULL,
    genre_id INTEGER NOT NULL,
    PRIMARY KEY (book_id, genre_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_books (
    user_book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    book_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'WANT_TO_READ'
        CHECK (status IN ('WANT_TO_READ', 'READING', 'READ', 'PAUSED', 'DROPPED')),
    rating INTEGER CHECK (rating IS NULL OR rating BETWEEN 1 AND 5),
    review TEXT,
    added_at TEXT NOT NULL DEFAULT (datetime('now')),
    started_at TEXT,
    finished_at TEXT,
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reading_sessions (
    session_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_book_id INTEGER NOT NULL,
    read_date TEXT NOT NULL,
    minutes_read INTEGER NOT NULL DEFAULT 0 CHECK (minutes_read >= 0),
    pages_read INTEGER NOT NULL DEFAULT 0 CHECK (pages_read >= 0),
    note TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_book_id) REFERENCES user_books(user_book_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS book_recommendations (
    recommendation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    book_id INTEGER NOT NULL,
    reason TEXT NOT NULL,
    score REAL NOT NULL DEFAULT 0 CHECK (score >= 0),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_books_source_external
    ON books(source_id, external_id)
    WHERE source_id IS NOT NULL AND external_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_books_title
    ON books(title);

CREATE INDEX IF NOT EXISTS idx_user_books_user_status
    ON user_books(user_id, status);

CREATE INDEX IF NOT EXISTS idx_user_books_book
    ON user_books(book_id);

CREATE INDEX IF NOT EXISTS idx_reading_sessions_user_book_date
    ON reading_sessions(user_book_id, read_date);

CREATE INDEX IF NOT EXISTS idx_recommendations_user_score
    ON book_recommendations(user_id, score DESC);

CREATE VIEW IF NOT EXISTS v_user_library AS
SELECT
    ub.user_book_id,
    ub.user_id,
    ub.status,
    ub.rating,
    ub.review,
    ub.added_at,
    ub.started_at,
    ub.finished_at,
    b.book_id,
    b.title,
    b.subtitle,
    b.description,
    b.page_count,
    b.thumbnail_url,
    b.preview_link,
    bs.code AS source_code,
    bs.name AS source_name
FROM user_books ub
JOIN books b ON b.book_id = ub.book_id
LEFT JOIN book_sources bs ON bs.source_id = b.source_id;

CREATE VIEW IF NOT EXISTS v_reading_calendar AS
SELECT
    ub.user_id,
    rs.read_date,
    SUM(rs.minutes_read) AS total_minutes,
    SUM(rs.pages_read) AS total_pages,
    CASE
        WHEN SUM(rs.minutes_read) > 0 OR SUM(rs.pages_read) > 0 THEN 1
        ELSE 0
    END AS is_reading_day
FROM reading_sessions rs
JOIN user_books ub ON ub.user_book_id = rs.user_book_id
GROUP BY ub.user_id, rs.read_date;

CREATE VIEW IF NOT EXISTS v_user_genre_stats AS
SELECT
    ub.user_id,
    g.genre_id,
    g.name AS genre_name,
    COUNT(*) AS books_count,
    AVG(ub.rating) AS average_rating
FROM user_books ub
JOIN book_genres bg ON bg.book_id = ub.book_id
JOIN genres g ON g.genre_id = bg.genre_id
WHERE ub.status = 'READ'
GROUP BY ub.user_id, g.genre_id, g.name;

INSERT OR IGNORE INTO book_sources (code, name, base_url) VALUES
    ('MANUAL', 'Manual input', NULL),
    ('OPEN_LIBRARY', 'Open Library API', 'https://openlibrary.org/'),
    ('OPEN_LIBRARY_HTML', 'Open Library HTML', 'https://openlibrary.org/search'),
    ('YANDEX_BOOKS_HTML', 'Yandex Books HTML', 'https://books.yandex.ru/');
