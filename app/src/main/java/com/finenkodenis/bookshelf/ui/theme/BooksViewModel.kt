package com.finenkodenis.bookshelf.ui.theme

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.finenkodenis.bookshelf.BooksApplication
import com.finenkodenis.bookshelf.data.AuthResult
import com.finenkodenis.bookshelf.data.Book
import com.finenkodenis.bookshelf.data.BookGenre
import com.finenkodenis.bookshelf.data.BooksRepository
import com.finenkodenis.bookshelf.data.DEMO_PASSWORD
import com.finenkodenis.bookshelf.data.DEMO_USERNAME
import com.finenkodenis.bookshelf.data.LibraryBook
import com.finenkodenis.bookshelf.data.LibraryRepository
import com.finenkodenis.bookshelf.data.LibraryStats
import com.finenkodenis.bookshelf.data.RecommendationEngine
import com.finenkodenis.bookshelf.data.User
import com.finenkodenis.bookshelf.data.UserRepository
import com.finenkodenis.bookshelf.data.local.ReadingStatus
import com.finenkodenis.bookshelf.data.mainBookGenres
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface BooksUiState {
    data class Success(val bookSearch: List<Book>) : BooksUiState
    data class Error(val message: String = "Не удалось загрузить книги") : BooksUiState
    object Loading : BooksUiState
}

enum class SearchWidgetState {
    OPENED,
    CLOSED
}

enum class AppSection(val title: String) {
    SEARCH("Поиск"),
    LIBRARY("Библиотека"),
    RECOMMENDATIONS("Рекомендации"),
    STATS("Статистика"),
    DETAIL("Книга")
}

@OptIn(ExperimentalCoroutinesApi::class)
class BooksViewModel(
    private val booksRepository: BooksRepository,
    private val libraryRepository: LibraryRepository,
    private val userRepository: UserRepository,
    private val recommendationEngine: RecommendationEngine = RecommendationEngine()
) : ViewModel() {

    var booksUiState: BooksUiState by mutableStateOf(BooksUiState.Loading)
        private set

    var recommendationsUiState: BooksUiState by mutableStateOf(BooksUiState.Success(emptyList()))
        private set

    var currentUser: User? by mutableStateOf(null)
        private set

    var authError: String? by mutableStateOf(null)
        private set

    var currentSection: AppSection by mutableStateOf(AppSection.SEARCH)
        private set

    var selectedBook: Book? by mutableStateOf(null)
        private set

    var selectedLibraryBook: LibraryBook? by mutableStateOf(null)
        private set

    private val currentUserId = MutableStateFlow<Long?>(null)
    private val libraryFilter = MutableStateFlow<ReadingStatus?>(null)

    val libraryStatusFilter: StateFlow<ReadingStatus?> = libraryFilter

    val libraryBooks: StateFlow<List<LibraryBook>> =
        combine(currentUserId, libraryFilter) { userId, filter -> userId to filter }
            .flatMapLatest { (userId, filter) ->
                if (userId == null) flowOf(emptyList()) else libraryRepository.observeLibrary(userId, filter)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allLibraryBooks: StateFlow<List<LibraryBook>> =
        currentUserId
            .flatMapLatest { userId ->
                if (userId == null) flowOf(emptyList()) else libraryRepository.observeLibrary(userId)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val libraryStats: StateFlow<LibraryStats> =
        currentUserId
            .flatMapLatest { userId ->
                if (userId == null) flowOf(LibraryStats()) else libraryRepository.observeStats(userId)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryStats())

    private val _searchWidgetState = mutableStateOf(SearchWidgetState.CLOSED)
    val searchWidgetState: State<SearchWidgetState> = _searchWidgetState

    private val _searchTextState = mutableStateOf("")
    val searchTextState: State<String> = _searchTextState

    val genres: List<BookGenre> = mainBookGenres

    init {
        getBooks()
    }

    fun updateSearchWidgetState(newValue: SearchWidgetState) {
        _searchWidgetState.value = newValue
    }

    fun updateSearchTextState(newValue: String) {
        _searchTextState.value = newValue
    }

    fun selectSection(section: AppSection) {
        currentSection = section
        if (section == AppSection.RECOMMENDATIONS) {
            loadRecommendations()
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            try {
                when (val result = userRepository.register(username, password)) {
                    is AuthResult.Success -> onAuthSuccess(result.user)
                    is AuthResult.Error -> authError = result.message
                }
            } catch (e: Exception) {
                authError = "Не удалось зарегистрироваться: ${e.message ?: "ошибка приложения"}"
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                when (val result = userRepository.login(username, password)) {
                    is AuthResult.Success -> onAuthSuccess(result.user)
                    is AuthResult.Error -> authError = result.message
                }
            } catch (e: Exception) {
                authError = "Не удалось войти: ${e.message ?: "ошибка приложения"}"
            }
        }
    }

    fun loginDemo() {
        viewModelScope.launch {
            try {
                when (val result = userRepository.loginDemo()) {
                    is AuthResult.Success -> onAuthSuccess(result.user)
                    is AuthResult.Error -> authError = result.message
                }
            } catch (e: Exception) {
                authError = "Не удалось войти в демо-режим: ${e.message ?: "ошибка приложения"}"
            }
        }
    }

    fun logout() {
        currentUser = null
        currentUserId.value = null
        authError = null
        selectedBook = null
        selectedLibraryBook = null
        currentSection = AppSection.SEARCH
    }

    fun setLibraryFilter(status: ReadingStatus?) {
        libraryFilter.value = status
    }

    fun openBook(book: Book, libraryBook: LibraryBook? = null) {
        selectedBook = book
        selectedLibraryBook = libraryBook
        currentSection = AppSection.DETAIL
    }

    fun closeBookDetails() {
        selectedBook = null
        selectedLibraryBook = null
        currentSection = AppSection.SEARCH
    }

    fun getBooks(query: String = "book", maxResults: Int = 40) {
        viewModelScope.launch {
            booksUiState = BooksUiState.Loading
            booksUiState =
                try {
                    BooksUiState.Success(booksRepository.getBooks(query, maxResults))
                } catch (e: IOException) {
                    BooksUiState.Error()
                } catch (e: HttpException) {
                    BooksUiState.Error()
                }
        }
    }

    fun searchByGenre(genre: BookGenre) {
        _searchTextState.value = genre.title
        getBooks(genre.query)
    }

    fun saveSelectedBook(status: ReadingStatus, rating: Int?, review: String?) {
        val user = currentUser ?: return
        val book = selectedBook ?: return

        viewModelScope.launch {
            val existing = selectedLibraryBook
            if (existing == null) {
                libraryRepository.addOrUpdateBook(
                    userId = user.id,
                    book = book,
                    status = status,
                    rating = rating,
                    review = review
                )
            } else {
                libraryRepository.updateLibraryBook(
                    userBookId = existing.userBookId,
                    status = status,
                    rating = rating,
                    review = review
                )
            }
            currentSection = AppSection.LIBRARY
            selectedBook = null
            selectedLibraryBook = null
        }
    }

    fun addReadingSession(userBookId: Long, minutesRead: Int, pagesRead: Int) {
        viewModelScope.launch {
            libraryRepository.addReadingSession(
                userBookId = userBookId,
                minutesRead = minutesRead,
                pagesRead = pagesRead
            )
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            recommendationsUiState = BooksUiState.Loading
            val library = allLibraryBooks.value
            val queries = recommendationEngine.recommendationQueries(libraryRepository.topGenres(library))
            val candidates = mutableListOf<Book>()

            try {
                queries.forEach { query ->
                    candidates += booksRepository.getBooks(query, 12)
                }
                recommendationsUiState = BooksUiState.Success(
                    recommendationEngine.filterAlreadyAdded(candidates, library).take(24)
                )
            } catch (e: IOException) {
                recommendationsUiState = BooksUiState.Error("Не удалось загрузить рекомендации")
            } catch (e: HttpException) {
                recommendationsUiState = BooksUiState.Error("Не удалось загрузить рекомендации")
            }
        }
    }

    private fun onAuthSuccess(user: User) {
        currentUser = user
        currentUserId.value = user.id
        authError = null
        currentSection = AppSection.SEARCH
    }

    companion object {
        const val DEMO_LOGIN = DEMO_USERNAME
        const val DEMO_PASS = DEMO_PASSWORD

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as BooksApplication)
                BooksViewModel(
                    booksRepository = application.container.booksRepository,
                    libraryRepository = application.container.libraryRepository,
                    userRepository = application.container.userRepository
                )
            }
        }
    }
}
