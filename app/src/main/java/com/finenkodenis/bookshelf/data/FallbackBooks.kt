package com.finenkodenis.bookshelf.data

private val fallbackBooks = listOf(
    Book(
        externalId = "fallback-war-and-peace",
        source = MANUAL_SOURCE,
        title = "War and Peace",
        authors = listOf("Лев Толстой"),
        description = "Роман-эпопея о русском обществе в эпоху наполеоновских войн.",
        categories = listOf("Классика", "История", "Роман"),
        publishedDate = "1869",
        imageLink = "https://covers.openlibrary.org/b/id/12621906-L.jpg",
        previewLink = "https://openlibrary.org/works/OL267035W"
    ),
    Book(
        externalId = "fallback-dune",
        source = MANUAL_SOURCE,
        title = "Dune",
        authors = listOf("Frank Herbert"),
        description = "Фантастический роман о политике, религии и власти на планете Арракис.",
        categories = listOf("Фантастика", "Приключения"),
        publishedDate = "1965",
        imageLink = "https://covers.openlibrary.org/b/id/12643362-L.jpg",
        previewLink = "https://openlibrary.org/works/OL893415W"
    ),
    Book(
        externalId = "fallback-1984",
        source = MANUAL_SOURCE,
        title = "1984",
        authors = listOf("George Orwell"),
        description = "Антиутопия о тоталитарном обществе и контроле над личностью.",
        categories = listOf("Классика", "Фантастика", "Антиутопия"),
        publishedDate = "1949",
        imageLink = "https://covers.openlibrary.org/b/id/7222246-L.jpg",
        previewLink = "https://openlibrary.org/works/OL1168083W"
    ),
    Book(
        externalId = "fallback-sherlock",
        source = MANUAL_SOURCE,
        title = "The Adventures of Sherlock Holmes",
        authors = listOf("Arthur Conan Doyle"),
        description = "Сборник детективных рассказов о Шерлоке Холмсе и докторе Ватсоне.",
        categories = listOf("Детективы", "Классика"),
        publishedDate = "1892",
        imageLink = "https://covers.openlibrary.org/b/id/8231856-L.jpg",
        previewLink = "https://openlibrary.org/works/OL262758W"
    ),
    Book(
        externalId = "fallback-harry-potter",
        source = MANUAL_SOURCE,
        title = "Harry Potter and the Philosopher's Stone",
        authors = listOf("J. K. Rowling"),
        description = "Первая книга о юном волшебнике Гарри Поттере.",
        categories = listOf("Фэнтези", "Детские книги", "Приключения"),
        publishedDate = "1997",
        imageLink = "https://covers.openlibrary.org/b/id/10521270-L.jpg",
        previewLink = "https://openlibrary.org/works/OL82563W"
    ),
    Book(
        externalId = "fallback-hobbit",
        source = MANUAL_SOURCE,
        title = "The Hobbit",
        authors = listOf("J. R. R. Tolkien"),
        description = "Приключение Бильбо Бэггинса в мире Средиземья.",
        categories = listOf("Фэнтези", "Приключения"),
        publishedDate = "1937",
        imageLink = "https://covers.openlibrary.org/b/id/6979861-L.jpg",
        previewLink = "https://openlibrary.org/works/OL262758W"
    ),
    Book(
        externalId = "fallback-psychology-money",
        source = MANUAL_SOURCE,
        title = "The Psychology of Money",
        authors = listOf("Morgan Housel"),
        description = "Книга о финансовом поведении, привычках и принятии решений.",
        categories = listOf("Психология", "Бизнес"),
        publishedDate = "2020",
        imageLink = "https://covers.openlibrary.org/b/id/10389354-L.jpg",
        previewLink = "https://openlibrary.org/works/OL20607008W"
    ),
    Book(
        externalId = "fallback-clean-code",
        source = MANUAL_SOURCE,
        title = "Clean Code",
        authors = listOf("Robert C. Martin"),
        description = "Практическое руководство по написанию понятного и сопровождаемого кода.",
        categories = listOf("Программирование"),
        publishedDate = "2008",
        imageLink = "https://covers.openlibrary.org/b/id/9610697-L.jpg",
        previewLink = "https://openlibrary.org/works/OL21536351W"
    )
)

fun fallbackBooksForQuery(query: String, maxResults: Int): List<Book> {
    val normalizedQuery = query
        .removePrefix("subject:")
        .trim()
        .lowercase()

    val result = fallbackBooks.filter { book ->
        normalizedQuery.isBlank() ||
            normalizedQuery == "book" ||
            book.title.lowercase().contains(normalizedQuery) ||
            book.authors.any { it.lowercase().contains(normalizedQuery) } ||
            book.categories.any { it.lowercase().contains(normalizedQuery) }
    }

    return result.ifEmpty { fallbackBooks }.take(maxResults)
}
