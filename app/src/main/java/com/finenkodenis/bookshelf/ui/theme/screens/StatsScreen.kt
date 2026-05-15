package com.finenkodenis.bookshelf.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.finenkodenis.bookshelf.data.LibraryBook
import com.finenkodenis.bookshelf.data.LibraryStats
import com.finenkodenis.bookshelf.data.local.ReadingStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

@Composable
fun StatsScreen(
    stats: LibraryStats,
    libraryBooks: List<LibraryBook>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Всего", stats.totalBooks.toString(), Modifier.weight(1f))
            StatCard("Прочитано", stats.readBooks.toString(), Modifier.weight(1f))
            StatCard("Читаю", stats.readingBooks.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Хочу", stats.wantToReadBooks.toString(), Modifier.weight(1f))
            StatCard("Минут", stats.totalMinutes.toString(), Modifier.weight(1f))
            StatCard("Страниц", stats.totalPages.toString(), Modifier.weight(1f))
        }
        StatCard(
            title = "Средняя оценка",
            value = stats.averageRating?.let { "%.1f / 5".format(Locale.US, it) } ?: "Нет оценок",
            modifier = Modifier.fillMaxWidth()
        )

        StatusChart(stats.statusCounts)
        ReadingCalendar(stats)
        GenreChart(stats)

        if (libraryBooks.isEmpty()) {
            Text(
                text = "Статистика появится после добавления книг в библиотеку.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun StatusChart(statusCounts: Map<ReadingStatus, Int>) {
    val maxCount = max(1, statusCounts.values.maxOrNull() ?: 0)
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text("Книги по статусам", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            ReadingStatus.values().forEach { status ->
                val count = statusCounts[status] ?: 0
                Text("${status.title}: $count", style = MaterialTheme.typography.bodyMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(count.toFloat() / maxCount)
                            .height(10.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReadingCalendar(stats: LibraryStats) {
    val activeDays = stats.readingDays.associateBy { it.date }
    val days = lastCalendarDates(30)
    val weekdayLabels = days.take(7).map { it.weekdayShort }

    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text("Календарь чтения за 30 дней", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Период: ${days.first().displayDate} - ${days.last().displayDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                weekdayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.size(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            days.chunked(7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.forEach { date ->
                        val day = activeDays[date.isoDate]
                        val hasReading = day != null && (day.totalMinutes > 0 || day.totalPages > 0)
                        val color = if (hasReading) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textColor = if (hasReading) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        CalendarDayCell(
                            dayNumber = date.dayOfMonth,
                            isToday = date.isToday,
                            color = color,
                            textColor = textColor,
                            hasReading = hasReading
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = "Темный день - было чтение, светлый - без чтения. Сегодня выделен рамкой.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    isToday: Boolean,
    color: Color,
    textColor: Color,
    hasReading: Boolean
) {
    val shape = RoundedCornerShape(6.dp)
    val cellModifier = Modifier
        .size(38.dp)
        .background(color, shape)
        .then(
            if (isToday) {
                Modifier.border(2.dp, MaterialTheme.colorScheme.tertiary, shape)
            } else {
                Modifier
            }
        )

    Box(
        modifier = cellModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber.toString(),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (hasReading) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun GenreChart(stats: LibraryStats) {
    if (stats.topGenres.isEmpty()) return

    val maxCount = max(1, stats.topGenres.maxOf { it.count })
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text("Популярные жанры", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            stats.topGenres.forEach { genre ->
                Text("${genre.genre}: ${genre.count}", style = MaterialTheme.typography.bodyMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(genre.count.toFloat() / maxCount)
                            .height(10.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

internal data class CalendarDate(
    val isoDate: String,
    val dayOfMonth: Int,
    val displayDate: String,
    val weekdayShort: String,
    val isToday: Boolean
)

internal fun lastCalendarDates(
    count: Int,
    today: Calendar = Calendar.getInstance()
): List<CalendarDate> {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val displayFormatter = SimpleDateFormat("d MMM", Locale("ru", "RU"))
    val weekdayFormatter = SimpleDateFormat("EE", Locale("ru", "RU"))
    val todayIso = formatter.format(today.time)
    val calendar = today.clone() as Calendar
    calendar.add(Calendar.DAY_OF_YEAR, -(count - 1))
    return (0 until count).map {
        val isoDate = formatter.format(calendar.time)
        CalendarDate(
            isoDate = isoDate,
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
            displayDate = displayFormatter.format(calendar.time),
            weekdayShort = weekdayFormatter.format(calendar.time).take(2).uppercase(Locale("ru", "RU")),
            isToday = isoDate == todayIso
        ).also {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}
