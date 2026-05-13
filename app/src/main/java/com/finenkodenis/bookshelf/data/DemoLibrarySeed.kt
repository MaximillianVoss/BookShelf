package com.finenkodenis.bookshelf.data

import com.finenkodenis.bookshelf.data.local.ReadingStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DemoLibrarySeed {
    const val SESSION_NOTE = "Демо-сессия чтения"

    val books: List<DemoBookSeed> = listOf(
        DemoBookSeed(
            book = Book(
                externalId = "demo-dune",
                source = MANUAL_SOURCE,
                title = "Dune",
                authors = listOf("Frank Herbert"),
                description = "Science fiction novel about politics, ecology and power on Arrakis.",
                categories = listOf("Science Fiction", "Adventure"),
                publishedDate = "1965",
                pageCount = 688,
                language = "en",
                previewLink = "https://openlibrary.org/works/OL893415W",
                imageLink = "https://covers.openlibrary.org/b/id/12073065-M.jpg"
            ),
            status = ReadingStatus.READ,
            rating = 5,
            review = "Понравились мир, политика и масштаб истории."
        ),
        DemoBookSeed(
            book = Book(
                externalId = "demo-foundation",
                source = MANUAL_SOURCE,
                title = "Foundation",
                authors = listOf("Isaac Asimov"),
                description = "A classic science fiction cycle about psychohistory and the fall of empire.",
                categories = listOf("Science Fiction", "Classic"),
                publishedDate = "1951",
                pageCount = 255,
                language = "en",
                previewLink = "https://openlibrary.org/works/OL45804W",
                imageLink = "https://covers.openlibrary.org/b/id/12613084-M.jpg"
            ),
            status = ReadingStatus.READ,
            rating = 5,
            review = "Хорошая база для рекомендаций по фантастике."
        ),
        DemoBookSeed(
            book = Book(
                externalId = "demo-hobbit",
                source = MANUAL_SOURCE,
                title = "The Hobbit",
                authors = listOf("J. R. R. Tolkien"),
                description = "Fantasy adventure about Bilbo Baggins and the journey to the Lonely Mountain.",
                categories = listOf("Fantasy", "Adventure"),
                publishedDate = "1937",
                pageCount = 310,
                language = "en",
                previewLink = "https://openlibrary.org/works/OL262758W",
                imageLink = "https://covers.openlibrary.org/b/id/6979861-M.jpg"
            ),
            status = ReadingStatus.READ,
            rating = 5,
            review = "Легкое приключение, хорошо показывает интерес к фэнтези."
        ),
        DemoBookSeed(
            book = Book(
                externalId = "demo-harry-potter",
                source = MANUAL_SOURCE,
                title = "Harry Potter and the Philosopher's Stone",
                authors = listOf("J. K. Rowling"),
                description = "A fantasy story about a young wizard beginning his studies at Hogwarts.",
                categories = listOf("Fantasy", "Young Adult"),
                publishedDate = "1997",
                pageCount = 223,
                language = "en",
                previewLink = "https://openlibrary.org/works/OL82563W",
                imageLink = "https://covers.openlibrary.org/b/id/10521270-M.jpg"
            ),
            status = ReadingStatus.READ,
            rating = 4,
            review = "Подходит для проверки жанра Fantasy."
        ),
        DemoBookSeed(
            book = Book(
                externalId = "demo-orient-express",
                source = MANUAL_SOURCE,
                title = "Murder on the Orient Express",
                authors = listOf("Agatha Christie"),
                description = "Detective mystery featuring Hercule Poirot and a famous train investigation.",
                categories = listOf("Detective", "Mystery"),
                publishedDate = "1934",
                pageCount = 256,
                language = "en",
                previewLink = "https://openlibrary.org/works/OL47159W",
                imageLink = "https://covers.openlibrary.org/b/id/8228691-M.jpg"
            ),
            status = ReadingStatus.READ,
            rating = 4,
            review = "Добавляет детективы в историю чтения."
        ),
        DemoBookSeed(
            book = Book(
                externalId = "demo-sapiens",
                source = MANUAL_SOURCE,
                title = "Sapiens",
                authors = listOf("Yuval Noah Harari"),
                description = "A nonfiction overview of human history and society.",
                categories = listOf("History", "Nonfiction"),
                publishedDate = "2011",
                pageCount = 464,
                language = "en",
                previewLink = "https://openlibrary.org/works/OL16813531W",
                imageLink = "https://covers.openlibrary.org/b/id/8370221-M.jpg"
            ),
            status = ReadingStatus.READ,
            rating = 4,
            review = "История и нон-фикшн для разнообразия рекомендаций."
        ),
        DemoBookSeed(
            book = Book(
                externalId = "demo-atomic-habits",
                source = MANUAL_SOURCE,
                title = "Atomic Habits",
                authors = listOf("James Clear"),
                description = "Practical nonfiction about building habits and improving everyday systems.",
                categories = listOf("Psychology", "Self-Help"),
                publishedDate = "2018",
                pageCount = 320,
                language = "en",
                previewLink = "https://openlibrary.org/works/OL17930368W",
                imageLink = "https://covers.openlibrary.org/b/id/10958382-M.jpg"
            ),
            status = ReadingStatus.READING,
            rating = null,
            review = "Книга в процессе чтения для проверки текущего статуса."
        ),
        DemoBookSeed(
            book = Book(
                externalId = "demo-clean-code",
                source = MANUAL_SOURCE,
                title = "Clean Code",
                authors = listOf("Robert C. Martin"),
                description = "A programming book about writing maintainable code.",
                categories = listOf("Programming", "Software Engineering"),
                publishedDate = "2008",
                pageCount = 464,
                language = "en",
                previewLink = "https://openlibrary.org/works/OL15170689W",
                imageLink = "https://covers.openlibrary.org/b/id/7279110-M.jpg"
            ),
            status = ReadingStatus.READING,
            rating = null,
            review = "Текущая книга по программированию."
        ),
        DemoBookSeed(
            book = Book(
                externalId = "demo-war-and-peace",
                source = MANUAL_SOURCE,
                title = "War and Peace",
                authors = listOf("Leo Tolstoy"),
                description = "A classic novel about Russian society during the Napoleonic era.",
                categories = listOf("Classic", "Novel"),
                publishedDate = "1869",
                pageCount = 1225,
                language = "en",
                previewLink = "https://openlibrary.org/works/OL267035W",
                imageLink = "https://covers.openlibrary.org/b/id/8231856-M.jpg"
            ),
            status = ReadingStatus.WANT_TO_READ,
            rating = null,
            review = null
        ),
        DemoBookSeed(
            book = Book(
                externalId = "demo-thinking-fast-slow",
                source = MANUAL_SOURCE,
                title = "Thinking, Fast and Slow",
                authors = listOf("Daniel Kahneman"),
                description = "A nonfiction book about cognitive biases and decision making.",
                categories = listOf("Psychology", "Nonfiction"),
                publishedDate = "2011",
                pageCount = 499,
                language = "en",
                previewLink = "https://openlibrary.org/works/OL16217185W",
                imageLink = "https://covers.openlibrary.org/b/id/7295661-M.jpg"
            ),
            status = ReadingStatus.WANT_TO_READ,
            rating = null,
            review = null
        )
    )

    fun monthSessions(baseTimeMillis: Long = System.currentTimeMillis()): List<DemoReadingSession> {
        val activeDaysAgo = listOf(29, 28, 27, 25, 24, 22, 21, 20, 17, 16, 14, 13, 11, 10, 9, 6, 5, 3, 2, 0)
        val bookIds = listOf(
            "demo-dune",
            "demo-foundation",
            "demo-hobbit",
            "demo-harry-potter",
            "demo-orient-express",
            "demo-sapiens",
            "demo-atomic-habits",
            "demo-clean-code"
        )
        val minutes = listOf(35, 25, 40, 30, 50, 20, 45, 60)
        val pages = listOf(22, 15, 28, 18, 30, 12, 24, 35)

        return activeDaysAgo.mapIndexed { index, daysAgo ->
            DemoReadingSession(
                bookExternalId = bookIds[index % bookIds.size],
                readDate = formatDateDaysAgo(baseTimeMillis, daysAgo),
                minutesRead = minutes[index % minutes.size],
                pagesRead = pages[index % pages.size]
            )
        }
    }

    private fun formatDateDaysAgo(baseTimeMillis: Long, daysAgo: Int): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance(Locale.US)
        calendar.timeInMillis = baseTimeMillis
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return formatter.format(calendar.time)
    }
}

data class DemoBookSeed(
    val book: Book,
    val status: ReadingStatus,
    val rating: Int?,
    val review: String?
)

data class DemoReadingSession(
    val bookExternalId: String,
    val readDate: String,
    val minutesRead: Int,
    val pagesRead: Int
)
