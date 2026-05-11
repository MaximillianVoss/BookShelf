package com.finenkodenis.bookshelf.data

import android.content.Context
import com.finenkodenis.bookshelf.data.local.BooksDatabase
import com.finenkodenis.bookshelf.network.model.BookService
import com.finenkodenis.bookshelf.network.model.OpenLibraryService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val booksRepository: BooksRepository
    val libraryRepository: LibraryRepository
    val userRepository: UserRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val googleBooksBaseUrl = "https://www.googleapis.com/books/v1/"
    private val openLibraryBaseUrl = "https://openlibrary.org/"

    private val database = BooksDatabase.getDatabase(context)

    private val googleBooksRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(googleBooksBaseUrl)
        .build()

    private val openLibraryRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(openLibraryBaseUrl)
        .build()

    private val googleBooksService: BookService by lazy {
        googleBooksRetrofit.create(BookService::class.java)
    }

    private val openLibraryService: OpenLibraryService by lazy {
        openLibraryRetrofit.create(OpenLibraryService::class.java)
    }

    override val booksRepository: BooksRepository by lazy {
        NetworkBooksRepository(googleBooksService, openLibraryService)
    }

    override val libraryRepository: LibraryRepository by lazy {
        LibraryRepository(
            bookDao = database.bookDao(),
            userBookDao = database.userBookDao(),
            readingSessionDao = database.readingSessionDao()
        )
    }

    override val userRepository: UserRepository by lazy {
        UserRepository(database.userDao())
    }
}
