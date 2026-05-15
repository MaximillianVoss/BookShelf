package com.finenkodenis.bookshelf.data

import android.content.Context
import com.finenkodenis.bookshelf.data.local.BooksDatabase
import com.finenkodenis.bookshelf.network.model.HtmlBookSearchService
import com.finenkodenis.bookshelf.network.model.OpenLibraryService
import com.finenkodenis.bookshelf.network.model.YandexBooksHtmlService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val booksRepository: BooksRepository
    val libraryRepository: LibraryRepository
    val userRepository: UserRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val openLibraryBaseUrl = "https://openlibrary.org/"
    private val yandexBooksBaseUrl = "https://books.yandex.ru/"

    private val database = BooksDatabase.getDatabase(context)

    private val openLibraryRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(openLibraryBaseUrl)
        .build()

    private val yandexBooksRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(yandexBooksBaseUrl)
        .build()

    private val openLibraryService: OpenLibraryService by lazy {
        openLibraryRetrofit.create(OpenLibraryService::class.java)
    }

    private val htmlBookSearchService: HtmlBookSearchService by lazy {
        openLibraryRetrofit.create(HtmlBookSearchService::class.java)
    }

    private val yandexBooksHtmlService: YandexBooksHtmlService by lazy {
        yandexBooksRetrofit.create(YandexBooksHtmlService::class.java)
    }

    override val booksRepository: BooksRepository by lazy {
        NetworkBooksRepository(
            openLibraryService = openLibraryService,
            htmlBookSearchService = htmlBookSearchService,
            yandexBooksHtmlService = yandexBooksHtmlService
        )
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
