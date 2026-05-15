package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.BookSearchSource
import com.finenkodenis.bookshelf.data.GOOGLE_BOOKS_HTML_SOURCE
import com.finenkodenis.bookshelf.data.NetworkBooksRepository
import com.finenkodenis.bookshelf.data.OPEN_LIBRARY_HTML_SOURCE
import com.finenkodenis.bookshelf.data.OPEN_LIBRARY_SOURCE
import com.finenkodenis.bookshelf.network.model.GoogleBooksHtmlService
import com.finenkodenis.bookshelf.network.model.HtmlBookSearchService
import com.finenkodenis.bookshelf.network.model.OpenLibraryDoc
import com.finenkodenis.bookshelf.network.model.OpenLibrarySearchResponse
import com.finenkodenis.bookshelf.network.model.OpenLibraryService
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class NetworkBooksRepositoryTest {

    @Test
    fun openLibrarySource_mapsApiFieldsToBook() = runTest {
        val repository = NetworkBooksRepository(
            openLibraryService = StaticOpenLibraryService()
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
    fun allSource_usesOpenLibraryApiAndKeepsResults() = runTest {
        val repository = NetworkBooksRepository(
            openLibraryService = StaticOpenLibraryService()
        )

        val books = repository.getBooks("hobbit", maxResults = 5, source = BookSearchSource.ALL)

        assertEquals("The Hobbit", books.single().title)
        assertEquals(OPEN_LIBRARY_SOURCE, books.single().source)
    }

    @Test
    fun allSource_usesHtmlParsersWhenOpenLibraryApiReturnsEmpty() = runTest {
        val repository = NetworkBooksRepository(
            openLibraryService = EmptyOpenLibraryService(),
            htmlBookSearchService = StaticHtmlBookSearchService(HTML_SEARCH_RESULT),
            googleBooksHtmlService = StaticGoogleBooksHtmlService(GOOGLE_BOOKS_HTML_SEARCH_RESULT)
        )

        val books = repository.getBooks("war and peace", maxResults = 2, source = BookSearchSource.ALL)

        assertEquals(listOf("War and Peace", "Google War and Peace"), books.map { it.title })
        assertEquals(listOf(OPEN_LIBRARY_HTML_SOURCE, GOOGLE_BOOKS_HTML_SOURCE), books.map { it.source })
    }

    @Test
    fun htmlParserSource_readsBooksFromSearchPageHtml() = runTest {
        val repository = NetworkBooksRepository(
            openLibraryService = EmptyOpenLibraryService(),
            htmlBookSearchService = StaticHtmlBookSearchService(HTML_SEARCH_RESULT)
        )

        val books = repository.getBooks("war and peace", maxResults = 5, source = BookSearchSource.HTML_PARSER)

        assertEquals("War and Peace", books.single().title)
        assertEquals(OPEN_LIBRARY_HTML_SOURCE, books.single().source)
    }

    @Test
    fun googleBooksHtmlSource_readsBooksFromSearchPageHtml() = runTest {
        val googleBooksService = StaticGoogleBooksHtmlService(GOOGLE_BOOKS_HTML_SEARCH_RESULT)
        val repository = NetworkBooksRepository(
            openLibraryService = EmptyOpenLibraryService(),
            googleBooksHtmlService = googleBooksService
        )

        val books = repository.getBooks("war and peace", maxResults = 5, source = BookSearchSource.GOOGLE_BOOKS_HTML)

        assertEquals("war and peace", googleBooksService.receivedQuery)
        assertEquals("Google War and Peace", books.single().title)
        assertEquals(GOOGLE_BOOKS_HTML_SOURCE, books.single().source)
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

    private class StaticGoogleBooksHtmlService(
        private val html: String
    ) : GoogleBooksHtmlService {
        var receivedQuery: String? = null

        override suspend fun searchBooksHtml(
            query: String,
            command: String,
            language: String
        ): Response<ResponseBody> {
            receivedQuery = query
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

        const val GOOGLE_BOOKS_HTML_SEARCH_RESULT = """
            <html>
              <body>
                <div class="book-result">
                  <h3>
                    <a href="/books?id=google-war-peace">Google War and Peace</a>
                  </h3>
                  <div class="book-author">By Google Author</div>
                </div>
              </body>
            </html>
        """
    }
}
