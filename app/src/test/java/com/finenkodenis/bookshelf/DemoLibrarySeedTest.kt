package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.DemoLibrarySeed
import com.finenkodenis.bookshelf.data.local.ReadingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class DemoLibrarySeedTest {
    @Test
    fun books_includeReadGenresForRecommendations() {
        val readBooks = DemoLibrarySeed.books.filter { it.status == ReadingStatus.READ }
        val genres = readBooks.flatMap { it.book.categories }

        assertTrue(readBooks.size >= 5)
        assertTrue("Science Fiction" in genres)
        assertTrue("Fantasy" in genres)
        assertTrue("Adventure" in genres)
    }

    @Test
    fun monthSessions_coverCurrentMonthWindowAndKnownBooks() {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val baseTime = formatter.parse("2026-05-13")!!.time
        val sessions = DemoLibrarySeed.monthSessions(baseTime)
        val bookIds = DemoLibrarySeed.books.mapNotNull { it.book.externalId }.toSet()

        assertEquals(20, sessions.size)
        assertEquals("2026-04-14", sessions.first().readDate)
        assertEquals("2026-05-13", sessions.last().readDate)
        assertTrue(sessions.all { it.bookExternalId in bookIds })
        assertTrue(sessions.all { it.minutesRead > 0 && it.pagesRead > 0 })
    }
}
