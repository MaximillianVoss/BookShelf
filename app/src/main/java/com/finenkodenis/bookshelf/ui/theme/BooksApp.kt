package com.finenkodenis.bookshelf.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finenkodenis.bookshelf.ui.theme.screens.AuthScreen
import com.finenkodenis.bookshelf.ui.theme.screens.BookDetailScreen
import com.finenkodenis.bookshelf.ui.theme.screens.LibraryScreen
import com.finenkodenis.bookshelf.ui.theme.screens.RecommendationsScreen
import com.finenkodenis.bookshelf.ui.theme.screens.SearchScreen
import com.finenkodenis.bookshelf.ui.theme.screens.StatsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksApp(modifier: Modifier = Modifier) {
    val viewModel: BooksViewModel = viewModel(factory = BooksViewModel.Factory)
    val currentUser = viewModel.currentUser
    val libraryBooks by viewModel.libraryBooks.collectAsState()
    val allLibraryBooks by viewModel.allLibraryBooks.collectAsState()
    val libraryStatusFilter by viewModel.libraryStatusFilter.collectAsState()
    val libraryStats by viewModel.libraryStats.collectAsState()

    if (currentUser == null) {
        AuthScreen(
            authError = viewModel.authError,
            onLogin = viewModel::login,
            onRegister = viewModel::register,
            demoUsername = BooksViewModel.DEMO_LOGIN,
            demoPassword = BooksViewModel.DEMO_PASS,
            onDemoLogin = viewModel::loginDemo,
            modifier = modifier.fillMaxSize()
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "${viewModel.currentSection.title} · ${currentUser.username}")
                },
                actions = {
                    TextButton(onClick = viewModel::logout) {
                        Text("Выйти")
                    }
                }
            )
        },
        bottomBar = {
            if (viewModel.currentSection != AppSection.DETAIL) {
                NavigationBar {
                    listOf(
                        AppSection.SEARCH,
                        AppSection.LIBRARY,
                        AppSection.RECOMMENDATIONS,
                        AppSection.STATS
                    ).forEach { section ->
                        NavigationBarItem(
                            selected = viewModel.currentSection == section,
                            onClick = { viewModel.selectSection(section) },
                            icon = {},
                            label = { Text(section.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (viewModel.currentSection) {
                AppSection.SEARCH -> SearchScreen(
                    booksUiState = viewModel.booksUiState,
                    searchText = viewModel.searchTextState.value,
                    onSearchTextChange = viewModel::updateSearchTextState,
                    onSearch = { viewModel.getBooks(it.ifBlank { "book" }) },
                    onBookClicked = { viewModel.openBook(it) },
                    retryAction = { viewModel.getBooks() }
                )
                AppSection.LIBRARY -> LibraryScreen(
                    libraryBooks = libraryBooks,
                    selectedStatus = libraryStatusFilter,
                    onStatusSelected = viewModel::setLibraryFilter,
                    onBookClicked = { viewModel.openBook(it.book, it) }
                )
                AppSection.RECOMMENDATIONS -> RecommendationsScreen(
                    booksUiState = viewModel.recommendationsUiState,
                    topGenres = libraryStats.topGenres,
                    onReload = viewModel::loadRecommendations,
                    onBookClicked = { viewModel.openBook(it) }
                )
                AppSection.STATS -> StatsScreen(
                    stats = libraryStats,
                    libraryBooks = allLibraryBooks
                )
                AppSection.DETAIL -> BookDetailScreen(
                    book = viewModel.selectedBook,
                    libraryBook = viewModel.selectedLibraryBook,
                    onBack = viewModel::closeBookDetails,
                    onSave = viewModel::saveSelectedBook,
                    onAddReadingSession = viewModel::addReadingSession
                )
            }
        }
    }
}
