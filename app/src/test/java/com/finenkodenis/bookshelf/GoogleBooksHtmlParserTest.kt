package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.GOOGLE_BOOKS_HTML_SOURCE
import com.finenkodenis.bookshelf.data.GoogleBooksHtmlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleBooksHtmlParserTest {
    @Test
    fun parseSearchResults_readsBooksFromGoogleBooksHtml() {
        val books = GoogleBooksHtmlParser.parseSearchResults(SAMPLE_HTML, limit = 5)

        assertEquals(1, books.size)
        assertEquals("google-book-1", books[0].externalId)
        assertEquals(GOOGLE_BOOKS_HTML_SOURCE, books[0].source)
        assertEquals("War and Peace", books[0].title)
        assertEquals(listOf("Leo Tolstoy"), books[0].authors)
        assertEquals("https://books.google.com/books?id=google-book-1&dq=war+and+peace", books[0].previewLink)
        assertEquals("https://books.google.com/books/content?id=google-book-1&printsec=frontcover&img=1&zoom=1", books[0].imageLink)
    }

    @Test
    fun parseSearchResults_ignoresJsOnlyGoogleSearchPage() {
        val books = GoogleBooksHtmlParser.parseSearchResults(JS_ONLY_HTML, limit = 5)

        assertTrue(books.isEmpty())
    }

    @Test
    fun parseSearchResults_ignoresRobotPage() {
        val books = GoogleBooksHtmlParser.parseSearchResults(ROBOT_HTML, limit = 5)

        assertTrue(books.isEmpty())
    }

    private companion object {
        const val SAMPLE_HTML = """
            <html>
              <body>
                <div class="book-result">
                  <a href="/books?id=google-book-1&dq=war+and+peace">
                    <img
                      src="/books/content?id=google-book-1&printsec=frontcover&img=1&zoom=1"
                      alt="War and Peace"
                    />
                  </a>
                  <h3>
                    <a href="/books?id=google-book-1&dq=war+and+peace">War and Peace</a>
                  </h3>
                  <div class="book-author">By Leo Tolstoy</div>
                  <div class="book-snippet">A classic novel about families and war.</div>
                </div>
              </body>
            </html>
        """

        const val JS_ONLY_HTML = """
            <html>
              <head><title>Google Search</title></head>
              <body>
                <noscript>
                  <meta content="0;url=/httpservice/retry/enablejs" http-equiv="refresh">
                </noscript>
              </body>
            </html>
        """

        const val ROBOT_HTML = """
            <html>
              <body>
                <p>Our systems have detected unusual traffic from your computer network.</p>
                <img src="//www.google.com/images/errors/robot.png">
              </body>
            </html>
        """
    }
}
