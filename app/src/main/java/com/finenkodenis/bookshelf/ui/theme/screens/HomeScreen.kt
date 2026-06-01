package com.finenkodenis.bookshelf.ui.theme.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.finenkodenis.bookshelf.data.Book
import com.finenkodenis.bookshelf.ui.theme.BooksUiState

@Composable
fun HomeScreen(
    booksUiState: BooksUiState,
    retryAction: () -> Unit,
    modifier: Modifier,
    onBookClicked: (Book) -> Unit
) {
    when (booksUiState) {
        is BooksUiState.Loading -> LoadingScreen(modifier)
        is BooksUiState.Success -> BooksGridScreen(
            books = booksUiState.bookSearch,
            modifier = modifier,
            onBookClicked = onBookClicked,
            canLoadMore = booksUiState.canLoadMore,
            isLoadingMore = booksUiState.isLoadingMore
        )
        is BooksUiState.Error -> ErrorScreen(retryAction = retryAction, modifier)
    }
}
