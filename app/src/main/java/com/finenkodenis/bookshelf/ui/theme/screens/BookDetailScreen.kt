package com.finenkodenis.bookshelf.ui.theme.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.finenkodenis.bookshelf.R
import com.finenkodenis.bookshelf.data.Book
import com.finenkodenis.bookshelf.data.LibraryBook
import com.finenkodenis.bookshelf.data.toSecureImageUrl
import com.finenkodenis.bookshelf.data.local.ReadingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Book?,
    libraryBook: LibraryBook?,
    onBack: () -> Unit,
    onSave: (ReadingStatus, Int?, String?) -> Unit,
    onAddReadingSession: (Long, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (book == null) {
        EmptyState(
            title = "Книга не выбрана",
            subtitle = "Вернитесь к поиску или библиотеке.",
            modifier = modifier
        )
        return
    }

    var status by remember { mutableStateOf(libraryBook?.status ?: ReadingStatus.WANT_TO_READ) }
    var rating by remember { mutableStateOf(libraryBook?.rating) }
    var review by remember { mutableStateOf(libraryBook?.review.orEmpty()) }
    var minutesRead by remember { mutableStateOf("20") }
    var pagesRead by remember { mutableStateOf("10") }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(book.localId, libraryBook?.userBookId) {
        status = libraryBook?.status ?: ReadingStatus.WANT_TO_READ
        rating = libraryBook?.rating
        review = libraryBook?.review.orEmpty()
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedButton(onClick = onBack) {
            Text("Назад")
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .width(128.dp)
                    .height(188.dp),
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(book.imageLink.toSecureImageUrl())
                    .crossfade(true)
                    .build(),
                error = painterResource(id = R.drawable.ic_book_96),
                placeholder = painterResource(id = R.drawable.loading_img),
                contentDescription = book.title,
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                if (book.authors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.authors.joinToString(", "),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                book.publishedDate?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Дата публикации: $it", style = MaterialTheme.typography.bodyMedium)
                }
                book.pageCount?.let {
                    Text("Страниц: $it", style = MaterialTheme.typography.bodyMedium)
                }
                Text("Источник: ${book.source}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Статус", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReadingStatus.values().forEach { item ->
                OutlinedButton(onClick = { status = item }) {
                    Text(if (status == item) "• ${item.title}" else item.title)
                }
            }
        }

        Text("Оценка", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..5).forEach { value ->
                OutlinedButton(onClick = { rating = value }) {
                    Text(if (rating == value) "★ $value" else value.toString())
                }
            }
        }

        OutlinedTextField(
            value = review,
            onValueChange = { review = it },
            label = { Text("Заметка или отзыв") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onSave(status, rating, review.ifBlank { null }) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (libraryBook == null) "Добавить в библиотеку" else "Сохранить изменения")
        }

        if (libraryBook != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("День чтения", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minutesRead,
                            onValueChange = { minutesRead = it.filter(Char::isDigit) },
                            label = { Text("Минуты") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pagesRead,
                            onValueChange = { pagesRead = it.filter(Char::isDigit) },
                            label = { Text("Страницы") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onAddReadingSession(
                                libraryBook.userBookId,
                                minutesRead.toIntOrNull() ?: 0,
                                pagesRead.toIntOrNull() ?: 0
                            )
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }

        if (book.categories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Жанры и темы", style = MaterialTheme.typography.titleMedium)
            Text(
                text = book.categories.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (!book.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Аннотация", style = MaterialTheme.typography.titleMedium)
            Text(text = book.description, style = MaterialTheme.typography.bodyMedium)
        }

        val previewLink = book.previewLink
        if (!previewLink.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { uriHandler.openUri(previewLink) }) {
                Text("Открыть страницу книги")
            }
        }
    }
}
