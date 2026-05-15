package com.finenkodenis.bookshelf.data

import android.content.Context
import com.finenkodenis.bookshelf.data.local.BooksDatabase
import com.finenkodenis.bookshelf.network.model.GutendexService
import com.finenkodenis.bookshelf.network.model.InternetArchiveService
import com.finenkodenis.bookshelf.network.model.LibraryOfCongressService
import com.finenkodenis.bookshelf.network.model.OpenLibraryService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val booksRepository: BooksRepository
    val libraryRepository: LibraryRepository
    val userRepository: UserRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val openLibraryBaseUrl = "https://openlibrary.org/"
    private val gutendexBaseUrl = "https://gutendex.com/"
    private val internetArchiveBaseUrl = "https://archive.org/"
    private val libraryOfCongressBaseUrl = "https://www.loc.gov/"

    private val database = BooksDatabase.getDatabase(context)

    private val openLibraryRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(openLibraryBaseUrl)
        .build()

    private val gutendexRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(gutendexBaseUrl)
        .build()

    private val internetArchiveRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(internetArchiveBaseUrl)
        .build()

    private val libraryOfCongressRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(libraryOfCongressBaseUrl)
        .build()

    private val openLibraryService: OpenLibraryService by lazy {
        openLibraryRetrofit.create(OpenLibraryService::class.java)
    }

    private val gutendexService: GutendexService by lazy {
        gutendexRetrofit.create(GutendexService::class.java)
    }

    private val internetArchiveService: InternetArchiveService by lazy {
        internetArchiveRetrofit.create(InternetArchiveService::class.java)
    }

    private val libraryOfCongressService: LibraryOfCongressService by lazy {
        libraryOfCongressRetrofit.create(LibraryOfCongressService::class.java)
    }

    override val booksRepository: BooksRepository by lazy {
        NetworkBooksRepository(
            openLibraryService = openLibraryService,
            gutendexService = gutendexService,
            internetArchiveService = internetArchiveService,
            libraryOfCongressService = libraryOfCongressService
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
