package com.finenkodenis.bookshelf.ui.theme.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finenkodenis.bookshelf.data.Book
import com.finenkodenis.bookshelf.data.GenreStat
import com.finenkodenis.bookshelf.ui.theme.BooksUiState

@Composable
fun RecommendationsScreen(
    booksUiState: BooksUiState,
    topGenres: List<GenreStat>,
    onReload: () -> Unit,
    onBookClicked: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text("Подборка по прочитанным жанрам", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (topGenres.isEmpty()) {
                    "Пока недостаточно прочитанных книг, показана базовая подборка."
                } else {
                    "Основные темы: ${topGenres.joinToString { "${it.genre} (${it.count})" }}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = onReload) {
                    Text("Обновить")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        when (booksUiState) {
            is BooksUiState.Loading -> LoadingScreen(Modifier.fillMaxSize())
            is BooksUiState.Success -> {
                if (booksUiState.bookSearch.isEmpty()) {
                    EmptyState(
                        title = "Рекомендаций пока нет",
                        subtitle = "Добавьте и оцените несколько прочитанных книг."
                    )
                } else {
                    BooksGridScreen(
                        books = booksUiState.bookSearch,
                        modifier = Modifier,
                        onBookClicked = onBookClicked
                    )
                }
            }
            is BooksUiState.Error -> ErrorScreen(retryAction = onReload, modifier = Modifier.fillMaxSize())
        }
    }
}
