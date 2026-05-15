package com.finenkodenis.bookshelf.ui.theme.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class ReadingCalendarDatesTest {

    @Test
    fun lastCalendarDates_returnsSequentialDatesEndingToday() {
        val today = GregorianCalendar(2026, Calendar.MAY, 15)

        val dates = lastCalendarDates(count = 3, today = today)

        assertEquals(listOf("2026-05-13", "2026-05-14", "2026-05-15"), dates.map { it.isoDate })
        assertEquals(listOf(13, 14, 15), dates.map { it.dayOfMonth })
        assertFalse(dates.first().isToday)
        assertTrue(dates.last().isToday)
    }
}
