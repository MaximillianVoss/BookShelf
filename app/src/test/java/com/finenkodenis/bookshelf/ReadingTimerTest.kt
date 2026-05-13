package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.ReadingTimer
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingTimerTest {
    @Test
    fun elapsedMinutes_roundsDownAndKeepsMinimumOneMinute() {
        assertEquals(1, ReadingTimer.elapsedMinutes(1_000L, 1_000L))
        assertEquals(1, ReadingTimer.elapsedMinutes(1_000L, 59_999L))
        assertEquals(2, ReadingTimer.elapsedMinutes(0L, 120_000L))
    }

    @Test
    fun elapsedMinutes_handlesClockMovingBackwards() {
        assertEquals(1, ReadingTimer.elapsedMinutes(10_000L, 5_000L))
    }

    @Test
    fun formatMinutes_usesRussianPluralForms() {
        assertEquals("1 минуту", ReadingTimer.formatMinutes(1))
        assertEquals("2 минуты", ReadingTimer.formatMinutes(2))
        assertEquals("5 минут", ReadingTimer.formatMinutes(5))
        assertEquals("11 минут", ReadingTimer.formatMinutes(11))
        assertEquals("21 минуту", ReadingTimer.formatMinutes(21))
    }
}
