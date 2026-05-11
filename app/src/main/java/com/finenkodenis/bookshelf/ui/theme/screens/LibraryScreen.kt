package com.finenkodenis.bookshelf.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.finenkodenis.bookshelf.data.LibraryBook
import com.finenkodenis.bookshelf.data.local.ReadingStatus

@Composable
fun LibraryScreen(
    libraryBooks: List<LibraryBook>,
    selectedStatus: ReadingStatus?,
    onStatusSelected: (ReadingStatus?) -> Unit,
    onBookClicked: (LibraryBook) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        StatusFilterRow(
            selectedStatus = selectedStatus,
            onStatusSelected = onStatusSelected
        )

        if (libraryBooks.isEmpty()) {
            EmptyState(
                title = "Библиотека пуста",
                subtitle = "Найдите книгу во вкладке поиска и добавьте ее в личный список."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(libraryBooks, key = { it.userBookId }) { libraryBook ->
                    LibraryBookCard(
                        libraryBook = libraryBook,
                        onClick = { onBookClicked(libraryBook) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusFilterRow(
    selectedStatus: ReadingStatus?,
    onStatusSelected: (ReadingStatus?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterButton(
            title = "Все",
            selected = selectedStatus == null,
            onClick = { onStatusSelected(null) }
        )
        ReadingStatus.values().forEach { status ->
            FilterButton(
                title = status.title,
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) }
            )
        }
    }
}

@Composable
private fun FilterButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(onClick = onClick) {
        Text(
            text = if (selected) "• $title" else title,
            maxLines = 1
        )
    }
}

@Composable
private fun LibraryBookCard(
    libraryBook: LibraryBook,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = libraryBook.book.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (libraryBook.book.authors.isNotEmpty()) {
                Text(
                    text = libraryBook.book.authors.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = libraryBook.status.title,
                    style = MaterialTheme.typography.bodyMedium
                )
                libraryBook.rating?.let {
                    Text(
                        text = "Оценка: $it/5",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
