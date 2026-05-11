package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.local.AppConverters
import com.finenkodenis.bookshelf.data.local.ReadingStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AppConvertersTest {
    private val converters = AppConverters()

    @Test
    fun stringListConverter_roundTripsValues() {
        val source = listOf("Fantasy", "History")

        val restored = converters.toStringList(converters.fromStringList(source))

        assertEquals(source, restored)
    }

    @Test
    fun readingStatusConverter_roundTripsValue() {
        val stored = converters.fromReadingStatus(ReadingStatus.READING)

        assertEquals(ReadingStatus.READING, converters.toReadingStatus(stored))
    }
}
