package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.YANDEX_BOOKS_HTML_SOURCE
import com.finenkodenis.bookshelf.data.YandexBooksHtmlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YandexBooksHtmlParserTest {
    @Test
    fun parseSearchResults_readsBooksFromYandexHtml() {
        val books = YandexBooksHtmlParser.parseSearchResults(SAMPLE_HTML, limit = 5)

        assertEquals(2, books.size)
        assertEquals("Война и мир", books[0].title)
        assertEquals(listOf("Лев Толстой"), books[0].authors)
        assertEquals("/books/TXGRits7", books[0].externalId)
        assertEquals("https://books.yandex.ru/books/TXGRits7", books[0].previewLink)
        assertEquals("https://api.bookmate.ru/assets/books-covers/ac/af/TXGRits7-ipad.jpeg", books[0].imageLink)
        assertEquals("ru", books[0].language)
        assertEquals(YANDEX_BOOKS_HTML_SOURCE, books[0].source)
    }

    @Test
    fun parseSearchResults_skipsNonBookSnippets() {
        val books = YandexBooksHtmlParser.parseSearchResults(SAMPLE_HTML_WITH_AUDIOBOOK, limit = 5)

        assertEquals(0, books.size)
    }

    @Test
    fun parseSearchResults_returnsNullOptionalFieldsWhenMissing() {
        val books = YandexBooksHtmlParser.parseSearchResults(SAMPLE_HTML_WITHOUT_COVER, limit = 5)

        assertEquals("Дюна", books.single().title)
        assertNull(books.single().imageLink)
    }

    private companion object {
        const val SAMPLE_HTML = """
            <html>
              <body>
                <div data-test-id="SNIPPET">
                  <div data-test-id="COVER">
                    <img src="https://api.bookmate.ru/assets/books-covers/ac/af/TXGRits7-ipad.jpeg" />
                  </div>
                  <div data-test-id="SNIPPET_TITLE">
                    <a href="/books/TXGRits7"><div>Война и мир</div></a>
                  </div>
                  <a data-test-id="SNIPPET_AUTHORS" href="/authors/M6LiiRGG">Лев Толстой</a>
                  <span data-test-id="SNIPPET_AUTHORS">и др.</span>
                  <div data-test-id="SNIPPET_DETAILS_BADGE">Книга</div>
                </div>
                <div data-test-id="SNIPPET">
                  <div data-test-id="SNIPPET_TITLE">
                    <a href="/books/S4rBgPLE"><div>Война и мир. Том 4</div></a>
                  </div>
                </div>
              </body>
            </html>
        """

        const val SAMPLE_HTML_WITH_AUDIOBOOK = """
            <html>
              <body>
                <div data-test-id="SNIPPET">
                  <div data-test-id="SNIPPET_TITLE">
                    <a href="/audiobooks/TXGRits7"><div>Война и мир</div></a>
                  </div>
                </div>
              </body>
            </html>
        """

        const val SAMPLE_HTML_WITHOUT_COVER = """
            <html>
              <body>
                <div data-test-id="SNIPPET">
                  <div data-test-id="SNIPPET_TITLE">
                    <a href="/books/dune42"><div>Дюна</div></a>
                  </div>
                </div>
              </body>
            </html>
        """
    }
}
