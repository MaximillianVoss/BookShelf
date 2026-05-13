package com.finenkodenis.bookshelf.ui.theme.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.finenkodenis.bookshelf.data.Book
import com.finenkodenis.bookshelf.data.BookGenre
import com.finenkodenis.bookshelf.data.BookSearchSource
import com.finenkodenis.bookshelf.ui.theme.BooksUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    booksUiState: BooksUiState,
    searchText: String,
    genres: List<BookGenre>,
    sources: List<BookSearchSource>,
    selectedSource: BookSearchSource,
    onSearchTextChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSourceSelected: (BookSearchSource) -> Unit,
    onGenreClicked: (BookGenre) -> Unit,
    onBookClicked: (Book) -> Unit,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                label = { Text("Название, автор или жанр") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(searchText) }),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onSearch(searchText) }) {
                Text("Найти")
            }
        }
        SourceCatalog(
            sources = sources,
            selectedSource = selectedSource,
            onSourceSelected = onSourceSelected,
            modifier = Modifier.fillMaxWidth()
        )
        GenreCatalog(
            genres = genres,
            onGenreClicked = onGenreClicked,
            modifier = Modifier.fillMaxWidth()
        )

        when (booksUiState) {
            is BooksUiState.Loading -> LoadingScreen(Modifier.fillMaxSize())
            is BooksUiState.Success -> BooksGridScreen(
                books = booksUiState.bookSearch,
                modifier = Modifier,
                onBookClicked = onBookClicked
            )
            is BooksUiState.Error -> ErrorScreen(retryAction = retryAction, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun SourceCatalog(
    sources: List<BookSearchSource>,
    selectedSource: BookSearchSource,
    onSourceSelected: (BookSearchSource) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)) {
        Text(
            text = "Источник книг",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
        ) {
            sources.forEach { source ->
                val selected = source == selectedSource
                val buttonModifier = Modifier.padding(end = 8.dp)
                if (selected) {
                    Button(
                        onClick = { onSourceSelected(source) },
                        modifier = buttonModifier
                    ) {
                        Text(text = source.title, maxLines = 1)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSourceSelected(source) },
                        modifier = buttonModifier
                    ) {
                        Text(text = source.title, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun GenreCatalog(
    genres: List<BookGenre>,
    onGenreClicked: (BookGenre) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)) {
        Text(
            text = "Основные жанры",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
        ) {
            genres.forEach { genre ->
                OutlinedButton(
                    onClick = { onGenreClicked(genre) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(text = genre.title, maxLines = 1)
                }
            }
        }
    }
}
