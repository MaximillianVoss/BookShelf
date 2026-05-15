package com.finenkodenis.bookshelf

import com.example.bookshelf.BookShelf
import com.example.bookshelf.ImageLinks
import com.example.bookshelf.Items
import com.example.bookshelf.VolumeInfo
import com.finenkodenis.bookshelf.data.BookSearchSource
import com.finenkodenis.bookshelf.data.GOOGLE_BOOKS_SOURCE
import com.finenkodenis.bookshelf.data.NetworkBooksRepository
import com.finenkodenis.bookshelf.data.OPEN_LIBRARY_HTML_SOURCE
import com.finenkodenis.bookshelf.data.OPEN_LIBRARY_SOURCE
import com.finenkodenis.bookshelf.data.YANDEX_BOOKS_HTML_SOURCE
import com.finenkodenis.bookshelf.network.model.BookService
import com.finenkodenis.bookshelf.network.model.HtmlBookSearchService
import com.finenkodenis.bookshelf.network.model.OpenLibraryDoc
import com.finenkodenis.bookshelf.network.model.OpenLibrarySearchResponse
import com.finenkodenis.bookshelf.network.model.OpenLibraryService
import com.finenkodenis.bookshelf.network.model.YandexBooksHtmlService
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class NetworkBooksRepositoryTest {

    @Test
    fun googleSource_passesConfiguredApiKey() = runTest {
        val googleService = CapturingGoogleBooksService(
            response = BookShelf(
                items = arrayListOf(
                    Items(
                        id = "google-1",
                        volumeInfo = VolumeInfo(
                            title = "Dune",
                            authors = arrayListOf("Frank Herbert"),
                            description = "Desert planet epic",
                            categories = arrayListOf("Science Fiction"),
                            publishedDate = "1965",
                            pageCount = 412,
                            language = "en",
                            previewLink = "https://books.google.com/books?id=google-1",
                            imageLinks = ImageLinks(thumbnail = "https://books.google.com/cover.jpg")
                        )
                    )
                )
            )
        )
        val repository = NetworkBooksRepository(
            bookService = googleService,
            openLibraryService = EmptyOpenLibraryService(),
            googleBooksApiKey = "test-google-key"
        )

        val books = repository.getBooks("dune", maxResults = 5, source = BookSearchSource.GOOGLE)

        assertEquals("test-google-key", googleService.receivedApiKey)
        val book = books.single()
        assertEquals("google-1", book.externalId)
        assertEquals("Dune", book.title)
        assertEquals(listOf("Frank Herbert"), book.authors)
        assertEquals("Desert planet epic", book.description)
        assertEquals(listOf("Science Fiction"), book.categories)
        assertEquals("1965", book.publishedDate)
        assertEquals(412, book.pageCount)
        assertEquals("en", book.language)
        assertEquals("https://books.google.com/books?id=google-1", book.previewLink)
        assertEquals("https://books.google.com/cover.jpg", book.imageLink)
        assertEquals(GOOGLE_BOOKS_SOURCE, book.source)
    }

    @Test
    fun openLibrarySource_mapsApiFieldsToBook() = runTest {
        val repository = NetworkBooksRepository(
            bookService = FailingGoogleBooksService(),
            openLibraryService = StaticOpenLibraryService(),
            googleBooksApiKey = ""
        )

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
    fun allSource_keepsOpenLibraryResultsWhenGoogleFails() = runTest {
        val repository = NetworkBooksRepository(
            bookService = FailingGoogleBooksService(),
            openLibraryService = StaticOpenLibraryService(),
            googleBooksApiKey = ""
        )

        val books = repository.getBooks("hobbit", maxResults = 5, source = BookSearchSource.ALL)

        assertEquals("The Hobbit", books.single().title)
        assertEquals(OPEN_LIBRARY_SOURCE, books.single().source)
    }

    @Test
    fun htmlParserSource_readsBooksFromSearchPageHtml() = runTest {
        val repository = NetworkBooksRepository(
            bookService = FailingGoogleBooksService(),
            openLibraryService = EmptyOpenLibraryService(),
            htmlBookSearchService = StaticHtmlBookSearchService(HTML_SEARCH_RESULT),
            googleBooksApiKey = ""
        )

        val books = repository.getBooks("war and peace", maxResults = 5, source = BookSearchSource.HTML_PARSER)

        assertEquals("War and Peace", books.single().title)
        assertEquals(OPEN_LIBRARY_HTML_SOURCE, books.single().source)
    }

    @Test
    fun yandexHtmlSource_readsBooksFromSearchPageHtml() = runTest {
        val yandexService = StaticYandexBooksHtmlService(YANDEX_HTML_SEARCH_RESULT)
        val repository = NetworkBooksRepository(
            bookService = FailingGoogleBooksService(),
            openLibraryService = EmptyOpenLibraryService(),
            yandexBooksHtmlService = yandexService,
            googleBooksApiKey = ""
        )

        val books = repository.getBooks("война и мир", maxResults = 5, source = BookSearchSource.YANDEX_HTML)

        assertEquals("%D0%B2%D0%BE%D0%B9%D0%BD%D0%B0%20%D0%B8%20%D0%BC%D0%B8%D1%80", yandexService.receivedEncodedQuery)
        assertEquals("Война и мир", books.single().title)
        assertEquals(YANDEX_BOOKS_HTML_SOURCE, books.single().source)
    }

    private class CapturingGoogleBooksService(
        private val response: BookShelf
    ) : BookService {
        var receivedApiKey: String? = null

        override suspend fun bookSearch(
            searchQuery: String,
            maxResults: Int,
            apiKey: String?
        ): BookShelf {
            receivedApiKey = apiKey
            return response
        }
    }

    private class FailingGoogleBooksService : BookService {
        override suspend fun bookSearch(
            searchQuery: String,
            maxResults: Int,
            apiKey: String?
        ): BookShelf {
            error("Google Books quota exceeded")
        }
    }

    private class EmptyOpenLibraryService : OpenLibraryService {
        override suspend fun searchBooks(query: String, limit: Int): OpenLibrarySearchResponse {
            return OpenLibrarySearchResponse()
        }
    }

    private class StaticOpenLibraryService : OpenLibraryService {
        override suspend fun searchBooks(query: String, limit: Int): OpenLibrarySearchResponse {
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

    private class StaticHtmlBookSearchService(
        private val html: String
    ) : HtmlBookSearchService {
        override suspend fun searchBooksHtml(query: String, layout: String): Response<ResponseBody> {
            return Response.success(html.toResponseBody("text/html".toMediaType()))
        }
    }

    private class StaticYandexBooksHtmlService(
        private val html: String
    ) : YandexBooksHtmlService {
        var receivedEncodedQuery: String? = null

        override suspend fun searchBooksHtml(encodedQuery: String): Response<ResponseBody> {
            receivedEncodedQuery = encodedQuery
            return Response.success(html.toResponseBody("text/html".toMediaType()))
        }
    }

    private companion object {
        const val HTML_SEARCH_RESULT = """
            <html>
              <body>
                <li class="searchResultItem">
                  <div class="resultTitle">
                    <h3 class="booktitle">
                      <a class="results" href="/works/OL267171W/War_and_Peace">War and Peace</a>
                    </h3>
                  </div>
                </li>
              </body>
            </html>
        """

        const val YANDEX_HTML_SEARCH_RESULT = """
            <html>
              <body>
                <div data-test-id="SNIPPET">
                  <div data-test-id="SNIPPET_TITLE">
                    <a href="/books/TXGRits7">Война и мир</a>
                  </div>
                </div>
              </body>
            </html>
        """
    }
}
