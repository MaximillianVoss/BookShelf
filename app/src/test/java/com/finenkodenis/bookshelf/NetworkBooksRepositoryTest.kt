package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.BookSearchSource
import com.finenkodenis.bookshelf.data.GUTENDEX_SOURCE
import com.finenkodenis.bookshelf.data.INTERNET_ARCHIVE_SOURCE
import com.finenkodenis.bookshelf.data.LIBRARY_OF_CONGRESS_SOURCE
import com.finenkodenis.bookshelf.data.NetworkBooksRepository
import com.finenkodenis.bookshelf.data.OPEN_LIBRARY_SOURCE
import com.finenkodenis.bookshelf.network.model.GutendexBook
import com.finenkodenis.bookshelf.network.model.GutendexPerson
import com.finenkodenis.bookshelf.network.model.GutendexSearchResponse
import com.finenkodenis.bookshelf.network.model.GutendexService
import com.finenkodenis.bookshelf.network.model.InternetArchiveDoc
import com.finenkodenis.bookshelf.network.model.InternetArchiveResponse
import com.finenkodenis.bookshelf.network.model.InternetArchiveSearchResponse
import com.finenkodenis.bookshelf.network.model.InternetArchiveService
import com.finenkodenis.bookshelf.network.model.LibraryOfCongressItem
import com.finenkodenis.bookshelf.network.model.LibraryOfCongressSearchResponse
import com.finenkodenis.bookshelf.network.model.LibraryOfCongressService
import com.finenkodenis.bookshelf.network.model.OpenLibraryDoc
import com.finenkodenis.bookshelf.network.model.OPEN_LIBRARY_SEARCH_FIELDS
import com.finenkodenis.bookshelf.network.model.OpenLibrarySearchResponse
import com.finenkodenis.bookshelf.network.model.OpenLibraryService
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkBooksRepositoryTest {

    @Test
    fun openLibrarySource_mapsApiFieldsToBook() = runTest {
        val repository = repository(openLibraryService = StaticOpenLibraryService())

        val books = repository.getBooks("hobbit", maxResults = 5, source = BookSearchSource.OPEN_LIBRARY)

        val book = books.single()
        assertEquals("/works/OL262758W", book.externalId)
        assertEquals("The Hobbit", book.title)
        assertEquals(listOf("J. R. R. Tolkien"), book.authors)
        assertEquals("In a hole in the ground there lived a hobbit.", book.description)
        assertEquals(listOf("Fantasy", "Adventure"), book.categories)
        assertEquals("1937", book.publishedDate)
        assertEquals("eng", book.language)
        assertEquals("https://openlibrary.org/works/OL262758W", book.previewLink)
        assertEquals("https://covers.openlibrary.org/b/id/12345-M.jpg", book.imageLink)
        assertEquals(OPEN_LIBRARY_SOURCE, book.source)
    }

    @Test
    fun genreQuery_addsRequestedGenreWhenApiDoesNotReturnSubjects() = runTest {
        val openLibraryService = CapturingOpenLibraryWithoutSubjectsService()
        val repository = repository(openLibraryService = openLibraryService)

        val books = repository.getBooks("subject:adventure", maxResults = 5, source = BookSearchSource.OPEN_LIBRARY)

        assertEquals("adventure", openLibraryService.receivedQuery)
        assertEquals(OPEN_LIBRARY_SEARCH_FIELDS, openLibraryService.receivedFields)
        assertEquals(listOf("Adventure"), books.single().categories)
    }

    @Test
    fun gutendexSource_mapsProjectGutenbergFieldsToBook() = runTest {
        val repository = repository(gutendexService = StaticGutendexService())

        val books = repository.getBooks("pride", maxResults = 5, source = BookSearchSource.GUTENDEX)

        val book = books.single()
        assertEquals("1342", book.externalId)
        assertEquals("Pride and Prejudice", book.title)
        assertEquals(listOf("Austen, Jane"), book.authors)
        assertEquals("A classic novel.", book.description)
        assertEquals(listOf("Courtship -- Fiction", "England -- Fiction", "Classics of Literature"), book.categories)
        assertEquals("en", book.language)
        assertEquals("https://www.gutenberg.org/files/1342/1342-h/1342-h.htm", book.previewLink)
        assertEquals("https://www.gutenberg.org/cache/epub/1342/pg1342.cover.medium.jpg", book.imageLink)
        assertEquals(GUTENDEX_SOURCE, book.source)
    }

    @Test
    fun internetArchiveSource_mapsArchiveFieldsToBook() = runTest {
        val archiveService = CapturingInternetArchiveService()
        val repository = repository(internetArchiveService = archiveService)

        val books = repository.getBooks("war and peace", maxResults = 5, source = BookSearchSource.INTERNET_ARCHIVE)

        assertEquals("mediatype:texts AND (title:\"war and peace\" OR creator:\"war and peace\" OR subject:\"war and peace\")", archiveService.receivedQuery)
        val book = books.single()
        assertEquals("warandpeace00tols", book.externalId)
        assertEquals("War and Peace", book.title)
        assertEquals(listOf("Tolstoy, Leo"), book.authors)
        assertEquals("Novel description", book.description)
        assertEquals(listOf("Russia", "Fiction", "War"), book.categories)
        assertEquals("1869", book.publishedDate)
        assertEquals("eng", book.language)
        assertEquals("https://archive.org/details/warandpeace00tols", book.previewLink)
        assertEquals("https://archive.org/services/img/warandpeace00tols", book.imageLink)
        assertEquals(INTERNET_ARCHIVE_SOURCE, book.source)
    }

    @Test
    fun libraryOfCongressSource_mapsLocFieldsToBook() = runTest {
        val repository = repository(libraryOfCongressService = StaticLibraryOfCongressService())

        val books = repository.getBooks("little women", maxResults = 5, source = BookSearchSource.LIBRARY_OF_CONGRESS)

        val book = books.single()
        assertEquals("https://www.loc.gov/item/12000001/", book.externalId)
        assertEquals("Little Women", book.title)
        assertEquals(listOf("Alcott, Louisa May"), book.authors)
        assertEquals("Digitized book description", book.description)
        assertEquals(listOf("Domestic fiction", "Sisters", "Mystery"), book.categories)
        assertEquals("1868", book.publishedDate)
        assertEquals("English", book.language)
        assertEquals("https://www.loc.gov/item/12000001/", book.previewLink)
        assertEquals("https://www.loc.gov/static/images/little-women.jpg", book.imageLink)
        assertEquals(LIBRARY_OF_CONGRESS_SOURCE, book.source)
    }

    @Test
    fun allSource_keepsWorkingSourcesWhenAnotherApiFails() = runTest {
        val repository = repository(
            openLibraryService = StaticOpenLibraryService(),
            gutendexService = FailingGutendexService(),
            internetArchiveService = CapturingInternetArchiveService(),
            libraryOfCongressService = StaticLibraryOfCongressService()
        )

        val books = repository.getBooks("hobbit", maxResults = 12, source = BookSearchSource.ALL)

        assertEquals(
            listOf(OPEN_LIBRARY_SOURCE, INTERNET_ARCHIVE_SOURCE, LIBRARY_OF_CONGRESS_SOURCE),
            books.map { it.source }
        )
    }

    private fun repository(
        openLibraryService: OpenLibraryService = EmptyOpenLibraryService(),
        gutendexService: GutendexService = EmptyGutendexService(),
        internetArchiveService: InternetArchiveService = EmptyInternetArchiveService(),
        libraryOfCongressService: LibraryOfCongressService = EmptyLibraryOfCongressService()
    ): NetworkBooksRepository {
        return NetworkBooksRepository(
            openLibraryService = openLibraryService,
            gutendexService = gutendexService,
            internetArchiveService = internetArchiveService,
            libraryOfCongressService = libraryOfCongressService
        )
    }

    private class EmptyOpenLibraryService : OpenLibraryService {
        override suspend fun searchBooks(
            query: String,
            limit: Int,
            fields: String
        ): OpenLibrarySearchResponse {
            return OpenLibrarySearchResponse()
        }
    }

    private class StaticOpenLibraryService : OpenLibraryService {
        override suspend fun searchBooks(
            query: String,
            limit: Int,
            fields: String
        ): OpenLibrarySearchResponse {
            return OpenLibrarySearchResponse(
                docs = listOf(
                    OpenLibraryDoc(
                        key = "/works/OL262758W",
                        title = "The Hobbit",
                        authors = listOf("J. R. R. Tolkien"),
                        firstPublishYear = 1937,
                        subjects = listOf("Fantasy", "Adventure"),
                        languages = listOf("eng"),
                        coverId = 12345,
                        firstSentence = listOf("In a hole in the ground there lived a hobbit.")
                    )
                )
            )
        }
    }

    private class CapturingOpenLibraryWithoutSubjectsService : OpenLibraryService {
        var receivedQuery: String? = null
        var receivedFields: String? = null

        override suspend fun searchBooks(
            query: String,
            limit: Int,
            fields: String
        ): OpenLibrarySearchResponse {
            receivedQuery = query
            receivedFields = fields
            return OpenLibrarySearchResponse(
                docs = listOf(
                    OpenLibraryDoc(
                        key = "/works/OLTEST",
                        title = "Adventure Book",
                        authors = listOf("Demo Author"),
                        subjects = emptyList()
                    )
                )
            )
        }
    }

    private class EmptyGutendexService : GutendexService {
        override suspend fun searchBooks(query: String): GutendexSearchResponse {
            return GutendexSearchResponse()
        }
    }

    private class StaticGutendexService : GutendexService {
        override suspend fun searchBooks(query: String): GutendexSearchResponse {
            return GutendexSearchResponse(
                results = listOf(
                    GutendexBook(
                        id = 1342,
                        title = "Pride and Prejudice",
                        authors = listOf(GutendexPerson("Austen, Jane")),
                        subjects = listOf("Courtship -- Fiction, England -- Fiction"),
                        bookshelves = listOf("Category: Classics of Literature"),
                        summaries = listOf("A classic novel."),
                        languages = listOf("en"),
                        formats = mapOf(
                            "text/html" to "https://www.gutenberg.org/files/1342/1342-h/1342-h.htm",
                            "image/jpeg" to "https://www.gutenberg.org/cache/epub/1342/pg1342.cover.medium.jpg"
                        )
                    )
                )
            )
        }
    }

    private class FailingGutendexService : GutendexService {
        override suspend fun searchBooks(query: String): GutendexSearchResponse {
            error("Gutendex unavailable")
        }
    }

    private class EmptyInternetArchiveService : InternetArchiveService {
        override suspend fun searchBooks(
            query: String,
            rows: Int,
            page: Int,
            output: String,
            fields: List<String>
        ): InternetArchiveSearchResponse {
            return InternetArchiveSearchResponse()
        }
    }

    private class CapturingInternetArchiveService : InternetArchiveService {
        var receivedQuery: String? = null

        override suspend fun searchBooks(
            query: String,
            rows: Int,
            page: Int,
            output: String,
            fields: List<String>
        ): InternetArchiveSearchResponse {
            receivedQuery = query
            return InternetArchiveSearchResponse(
                response = InternetArchiveResponse(
                    docs = listOf(
                        InternetArchiveDoc(
                            identifier = "warandpeace00tols",
                            title = JsonPrimitive("War and Peace"),
                            creator = JsonPrimitive("Tolstoy, Leo"),
                            description = JsonPrimitive("Novel description"),
                            subject = JsonPrimitive("Russia, Fiction; War"),
                            date = JsonPrimitive("1869"),
                            language = JsonPrimitive("eng")
                        )
                    )
                )
            )
        }
    }

    private class EmptyLibraryOfCongressService : LibraryOfCongressService {
        override suspend fun searchBooks(
            query: String,
            count: Int,
            format: String
        ): LibraryOfCongressSearchResponse {
            return LibraryOfCongressSearchResponse()
        }
    }

    private class StaticLibraryOfCongressService : LibraryOfCongressService {
        override suspend fun searchBooks(
            query: String,
            count: Int,
            format: String
        ): LibraryOfCongressSearchResponse {
            return LibraryOfCongressSearchResponse(
                results = listOf(
                    LibraryOfCongressItem(
                        title = "Little Women",
                        date = "1868",
                        url = "https://www.loc.gov/item/12000001/",
                        creator = JsonPrimitive("Alcott, Louisa May"),
                        description = jsonArrayOf("Digitized book description"),
                        subject = jsonArrayOf("Domestic fiction", "Sisters"),
                        genre = JsonPrimitive("Mystery"),
                        language = JsonPrimitive("English"),
                        imageUrl = listOf("https://www.loc.gov/static/images/little-women.jpg")
                    )
                )
            )
        }
    }

    private companion object {
        fun jsonArrayOf(vararg values: String): JsonArray {
            return JsonArray().apply {
                values.forEach { add(JsonPrimitive(it)) }
            }
        }
    }
}
