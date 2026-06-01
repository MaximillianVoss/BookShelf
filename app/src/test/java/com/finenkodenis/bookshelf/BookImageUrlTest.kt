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
    fun toSecureImageUrl_keepsLocalServerHttpUrlUnchanged() {
        val emulatorUrl = "http://10.0.2.2:8000/api/books/war-and-peace-demo/cover.png"
        val localhostUrl = "http://127.0.0.1:8000/api/books/alice-demo/cover.png"
        val lanUrl = "http://192.168.1.25:8000/api/books/comics-demo-01/cover.png"

        assertEquals(emulatorUrl, emulatorUrl.toSecureImageUrl())
        assertEquals(localhostUrl, localhostUrl.toSecureImageUrl())
        assertEquals(lanUrl, lanUrl.toSecureImageUrl())
    }

    @Test
    fun toSecureImageUrl_returnsNullForBlankValue() {
        assertNull("  ".toSecureImageUrl())
    }
}
