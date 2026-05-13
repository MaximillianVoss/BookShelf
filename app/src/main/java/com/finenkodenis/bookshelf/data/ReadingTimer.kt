package com.finenkodenis.bookshelf.data

object ReadingTimer {
    private const val MILLIS_IN_MINUTE = 60_000L

    fun elapsedMinutes(startedAtMillis: Long, finishedAtMillis: Long): Int {
        val elapsedMillis = (finishedAtMillis - startedAtMillis).coerceAtLeast(0)
        return (elapsedMillis / MILLIS_IN_MINUTE).toInt().coerceAtLeast(1)
    }

    fun formatMinutes(minutes: Int): String {
        val value = minutes.coerceAtLeast(1)
        val lastTwoDigits = value % 100
        val lastDigit = value % 10
        val suffix = if (lastTwoDigits in 11..14) {
            "минут"
        } else {
            when (lastDigit) {
                1 -> "минуту"
                in 2..4 -> "минуты"
                else -> "минут"
            }
        }
        return "$value $suffix"
    }
}
