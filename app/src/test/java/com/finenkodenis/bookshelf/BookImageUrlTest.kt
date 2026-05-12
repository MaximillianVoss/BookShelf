package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.toSecureImageUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookImageUrlTest {
    @Test
    fun toSecureImageUrl_keepsHttpsUrlUnchanged() {
        val url = "https://covers.openlibrary.org/b/id/12621906-M.jpg"

        assertEquals(url, url.toSecureImageUrl())
    }

    @Test
    fun toSecureImageUrl_convertsHttpUrlToHttps() {
        assertEquals(
            "https://books.google.com/books/content?id=1",
            "http://books.google.com/books/content?id=1".toSecureImageUrl()
        )
    }

    @Test
    fun toSecureImageUrl_returnsNullForBlankValue() {
        assertNull("  ".toSecureImageUrl())
    }
}
