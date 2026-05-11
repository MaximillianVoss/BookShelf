package com.finenkodenis.bookshelf.data

data class Book(
    val localId: Long? = null,
    val externalId: String? = null,
    val source: String = GOOGLE_BOOKS_SOURCE,
    val title: String,
    val authors: List<String> = emptyList(),
    val description: String? = null,
    val categories: List<String> = emptyList(),
    val publishedDate: String? = null,
    val pageCount: Int? = null,
    val language: String? = null,
    val previewLink: String? = null,
    val imageLink: String? = null
)

const val GOOGLE_BOOKS_SOURCE = "GOOGLE_BOOKS"
const val OPEN_LIBRARY_SOURCE = "OPEN_LIBRARY"
const val MANUAL_SOURCE = "MANUAL"
