package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.LOCAL_SERVER_SOURCE
import com.finenkodenis.bookshelf.data.toBookSourceLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class BookSourceLabelsTest {
    @Test
    fun localServerSource_hasUserFacingLabel() {
        assertEquals("Наш сервер", LOCAL_SERVER_SOURCE.toBookSourceLabel())
    }
}
