package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.OPEN_LIBRARY_HTML_SOURCE
import com.finenkodenis.bookshelf.data.OpenLibraryHtmlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenLibraryHtmlParserTest {
    @Test
    fun parseSearchResults_readsBooksFromOpenLibraryHtml() {
        val books = OpenLibraryHtmlParser.parseSearchResults(SAMPLE_HTML, limit = 5)

        assertEquals(2, books.size)
        assertEquals("War and Peace", books[0].title)
        assertEquals(listOf("Leo Tolstoy"), books[0].authors)
        assertEquals(listOf("Historical fiction", "War stories"), books[0].categories)
        assertEquals("/works/OL267171W", books[0].externalId)
        assertEquals("https://openlibrary.org/works/OL267171W/War_and_Peace?edition=key", books[0].previewLink)
        assertEquals("https://covers.openlibrary.org/b/id/15204463-M.jpg", books[0].imageLink)
        assertEquals(OPEN_LIBRARY_HTML_SOURCE, books[0].source)
    }

    @Test
    fun parseSearchResults_skipsItemsWithoutTitle() {
        val books = OpenLibraryHtmlParser.parseSearchResults(SAMPLE_HTML_WITHOUT_TITLE, limit = 5)

        assertEquals(0, books.size)
    }

    @Test
    fun parseSearchResults_returnsNullOptionalFieldsWhenMissing() {
        val books = OpenLibraryHtmlParser.parseSearchResults(SAMPLE_HTML_WITHOUT_COVER, limit = 5)

        assertEquals("Dune", books.single().title)
        assertNull(books.single().imageLink)
    }

    private companion object {
        const val SAMPLE_HTML = """
            <html>
              <body>
                <ul class="list-books">
                  <li class="searchResultItem">
                    <span class="bookcover">
                      <a href="/works/OL267171W/War_and_Peace?edition=key">
                        <img itemprop="image" src="//covers.openlibrary.org/b/id/15204463-M.jpg" />
                      </a>
                    </span>
                    <div class="details">
                      <div class="resultTitle">
                        <h3 class="booktitle">
                          <a class="results" href="/works/OL267171W/War_and_Peace?edition=key">War and Peace</a>
                        </h3>
                      </div>
                      <span class="bookauthor">by <a href="/authors/OL26783A/Leo_Tolstoy">Leo Tolstoy</a></span>
                      <ol-chip-group class="srw__subjects">
                        <ol-chip>Historical fiction</ol-chip>
                        <ol-chip>War stories</ol-chip>
                      </ol-chip-group>
                    </div>
                  </li>
                  <li class="searchResultItem">
                    <div class="resultTitle">
                      <h3 class="booktitle">
                        <a class="results" href="/works/OL123W/Peace">Peace</a>
                      </h3>
                    </div>
                  </li>
                </ul>
              </body>
            </html>
        """

        const val SAMPLE_HTML_WITHOUT_TITLE = """
            <html>
              <body>
                <li class="searchResultItem">
                  <span class="bookauthor">by <a>Unknown</a></span>
                </li>
              </body>
            </html>
        """

        const val SAMPLE_HTML_WITHOUT_COVER = """
            <html>
              <body>
                <li class="searchResultItem">
                  <div class="resultTitle">
                    <h3 class="booktitle">
                      <a class="results" href="/works/OL893415W/Dune">Dune</a>
                    </h3>
                  </div>
                </li>
              </body>
            </html>
        """
    }
}
