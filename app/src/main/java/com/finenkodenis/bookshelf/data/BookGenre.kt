package com.finenkodenis.bookshelf.data

data class BookGenre(
    val title: String,
    val query: String
)

val mainBookGenres = listOf(
    BookGenre("Фэнтези", "subject:fantasy"),
    BookGenre("Фантастика", "subject:science fiction"),
    BookGenre("Детективы", "subject:detective"),
    BookGenre("Романы", "subject:romance"),
    BookGenre("Классика", "subject:classic literature"),
    BookGenre("Ужасы", "subject:horror"),
    BookGenre("Приключения", "subject:adventure"),
    BookGenre("Психология", "subject:psychology"),
    BookGenre("Бизнес", "subject:business"),
    BookGenre("История", "subject:history"),
    BookGenre("Биографии", "subject:biography"),
    BookGenre("Программирование", "subject:programming"),
    BookGenre("Детские книги", "subject:children"),
    BookGenre("Комиксы", "subject:comics")
)
