# Описание основных классов BookShelf

Документ описывает основные классы Android-приложения BookShelf и их роль в проекте.

Базовый пакет приложения:

```text
com.finenkodenis.bookshelf
```

## Общая схема слоев

```text
MainActivity
    |
    v
BooksApp / Jetpack Compose screens
    |
    v
BooksViewModel
    |
    v
Repositories
    |
    +--> Room / SQLite
    |
    +--> Retrofit APIs
```

Приложение построено по простой MVVM-схеме:

- Compose-экраны отвечают за отображение и ввод пользователя.
- `BooksViewModel` хранит состояние экранов и вызывает бизнес-логику.
- Репозитории работают с БД, API и доменными объектами.
- Room-слой хранит пользователей, книги, пользовательскую библиотеку и сессии чтения.

## Точка входа приложения

### `MainActivity`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/MainActivity.kt
```

Главная Android Activity. Наследуется от `ComponentActivity`.

Основные задачи:

- запускает Compose UI;
- применяет тему приложения;
- вызывает корневой composable `BooksApp`.

`MainActivity` не содержит бизнес-логики. Это только точка входа для Android и Compose.

### `BooksApplication`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/BooksApplication.kt
```

Класс приложения, наследуется от `Application`.

Основные задачи:

- создает и хранит `AppContainer`;
- предоставляет зависимости для всего приложения;
- инициализируется раньше Activity.

Используется в `BooksViewModel.Factory`, чтобы получить репозитории из контейнера.

## Dependency container

### `AppContainer`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/AppContainer.kt
```

Интерфейс контейнера зависимостей.

Содержит основные зависимости:

```kotlin
val booksRepository: BooksRepository
val libraryRepository: LibraryRepository
val userRepository: UserRepository
```

Нужен для отделения создания объектов от их использования.

### `DefaultAppContainer`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/AppContainer.kt
```

Основная реализация `AppContainer`.

Создает:

- `BooksDatabase`;
- Retrofit-клиент для Google Books;
- Retrofit-клиент для Open Library;
- `NetworkBooksRepository`;
- `LibraryRepository`;
- `UserRepository`.

Именно здесь связаны сетевые сервисы, Room DAO и репозитории.

## ViewModel и состояние UI

### `BooksViewModel`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/BooksViewModel.kt
```

Главная ViewModel приложения.

Отвечает за:

- авторизацию;
- демо-вход;
- загрузку и поиск книг;
- переключение разделов приложения;
- выбор книги;
- добавление книг в библиотеку;
- изменение статусов, оценок и отзывов;
- запуск встроенного чтения;
- учет времени чтения;
- загрузку рекомендаций;
- выдачу потоков библиотеки и статистики для UI.

Ключевые состояния:

```kotlin
booksUiState
recommendationsUiState
currentUser
authError
currentSection
selectedBook
selectedLibraryBook
readerUrl
readerTitle
readerStartedAtMillis
```

Ключевые методы:

```kotlin
login(username, password)
loginDemo()
logout()
getBooks(query, maxResults)
searchByGenre(genre)
openBook(book, libraryBook)
saveSelectedBook(status, rating, review)
openReader(book)
closeReader()
saveReaderSession(minutesRead)
loadRecommendations()
```

`BooksViewModel` не работает напрямую с Room или Retrofit. Для этого используются репозитории.

### `BooksUiState`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/BooksViewModel.kt
```

Sealed interface для состояния загрузки списков книг.

Варианты:

```kotlin
Success(bookSearch)
Error(message)
Loading
```

Используется на экранах поиска и рекомендаций.

### `AppSection`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/BooksViewModel.kt
```

Enum с основными разделами приложения:

```text
SEARCH
LIBRARY
RECOMMENDATIONS
STATS
DETAIL
READER
```

Используется для навигации внутри одного `Scaffold`.

### `SearchWidgetState`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/BooksViewModel.kt
```

Enum состояния поискового виджета:

```text
OPENED
CLOSED
```

Сейчас используется как часть состояния поиска.

## UI-компоненты и экраны

### `BooksApp`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/BooksApp.kt
```

Корневой composable приложения.

Отвечает за:

- получение `BooksViewModel`;
- проверку, вошел ли пользователь;
- отображение `AuthScreen`, если пользователь не авторизован;
- основной `Scaffold` с верхней панелью и нижней навигацией;
- выбор нужного экрана по `AppSection`.

Основные экраны подключаются здесь:

```kotlin
SearchScreen
LibraryScreen
RecommendationsScreen
StatsScreen
BookDetailScreen
ReaderScreen
```

### `AuthScreen`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/screens/AuthScreen.kt
```

Экран входа и регистрации.

Позволяет:

- ввести логин и пароль;
- выполнить вход;
- зарегистрировать пользователя;
- выполнить демо-вход.

Демо-кнопка использует логин `demo` и пароль `demo1234`.

### `SearchScreen`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/screens/SearchScreen.kt
```

Экран поиска книг.

Показывает:

- поле поиска;
- кнопку поиска;
- выбор источника книг;
- быстрые жанровые фильтры;
- список найденных книг.

При выборе жанра вызывает `searchByGenre`, а при выборе книги открывает `BookDetailScreen`.

### `BooksGridScreen`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/screens/BooksGridScreen.kt
```

Переиспользуемый экран сетки книг.

Используется:

- в поиске;
- в рекомендациях.

Отвечает за отображение карточек книг с названием, автором и обложкой.

### `LibraryScreen`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/screens/LibraryScreen.kt
```

Экран личной библиотеки пользователя.

Показывает добавленные книги и фильтр по статусам.

Данные приходят из:

```kotlin
BooksViewModel.libraryBooks
```

### `BookDetailScreen`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/screens/BookDetailScreen.kt
```

Экран детальной информации о книге.

Показывает:

- обложку;
- название;
- авторов;
- дату публикации;
- источник;
- статус;
- оценку от 1 до 5;
- отзыв;
- блок ручного добавления дня чтения;
- жанры и темы;
- аннотацию;
- кнопку чтения внутри приложения.

Через этот экран пользователь добавляет книгу в библиотеку и обновляет данные о ней.

### `ReaderScreen`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/screens/ReaderScreen.kt
```

Экран встроенного чтения.

Использует:

```kotlin
AndroidView
WebView
WebViewClient
```

Основные задачи:

- открыть `previewLink` книги внутри приложения;
- перехватить кнопку назад;
- показать диалог `Засчитать чтение?`;
- передать рассчитанное время чтения во ViewModel.

Если пользователь подтверждает учет, вызывается:

```kotlin
onSaveReadingSession(minutes)
```

### `RecommendationsScreen`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/screens/RecommendationsScreen.kt
```

Экран рекомендаций.

Показывает:

- заголовок подборки;
- основные жанры пользователя;
- кнопку обновления;
- список рекомендованных книг.

Если у пользователя нет прочитанных книг, показывается базовая подборка.

### `StatsScreen`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/screens/StatsScreen.kt
```

Экран статистики чтения.

Показывает:

- общее количество книг;
- количество прочитанных книг;
- количество книг в процессе чтения;
- количество книг в списке `Хочу прочитать`;
- суммарные минуты;
- суммарные страницы;
- среднюю оценку;
- диаграмму статусов;
- календарь чтения за 30 дней;
- популярные жанры.

Данные приходят из:

```kotlin
BooksViewModel.libraryStats
```

### `LoadingScreen`, `ErrorScreen`, `EmptyState`

Файлы:

```text
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/screens/LoadingScreens.kt
app/src/main/java/com/finenkodenis/bookshelf/ui/theme/screens/ErrorScreens.kt
```

Вспомогательные UI-состояния:

- загрузка;
- ошибка;
- пустой результат.

## Репозитории

### `UserRepository`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/UserRepository.kt
```

Отвечает за пользователей и авторизацию.

Основные методы:

```kotlin
register(username, password)
login(username, password)
loginDemo()
```

Особенности:

- логин нормализуется через `trim`;
- пароль не хранится в открытом виде;
- используется `PasswordHasher`;
- демо-пользователь создается или обновляется при демо-входе.

### `AuthResult`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/UserRepository.kt
```

Sealed class результата авторизации.

Варианты:

```kotlin
Success(user)
Error(message)
```

### `BooksRepository`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/BooksRepository.kt
```

Интерфейс поиска книг.

Метод:

```kotlin
suspend fun getBooks(query: String, maxResults: Int, source: BookSearchSource): List<Book>
```

Параметр `source` задает источник поиска: все источники, только Google Books, только Open Library или локальный каталог.

### `NetworkBooksRepository`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/BooksRepository.kt
```

Реализация `BooksRepository`.

Работает с:

- `BookService` для Google Books;
- `OpenLibraryService` для Open Library;
- fallback-каталогом, если API не вернули результат.
- необязательным `googleBooksApiKey`, который передается в Google Books как параметр `key`.

Логика:

1. Нормализует поисковый запрос.
2. Выбирает источник по `BookSearchSource`.
3. Для режима `ALL` делит лимит результатов между Google Books и Open Library.
4. Выполняет сетевые запросы через Retrofit.
5. Преобразует API-модели в доменную модель `Book`.
6. Удаляет дубликаты.
7. Если общий список пустой, использует `fallbackBooksForQuery`.
8. Для конкретного выбранного источника не подменяет результат fallback-каталогом, чтобы пользователь видел реальное состояние выбранного API.

### `LibraryRepository`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/LibraryRepository.kt
```

Репозиторий личной библиотеки.

Работает с:

- `BookDao`;
- `UserBookDao`;
- `ReadingSessionDao`;
- `RecommendationEngine`.

Основные методы:

```kotlin
observeLibrary(userId, status)
observeLibraryBook(userBookId)
observeStats(userId)
addOrUpdateBook(userId, book, status, rating, review)
updateLibraryBook(userBookId, status, rating, review)
addReadingSession(userBookId, minutesRead, pagesRead, note, readDate)
ensureBookForReading(userId, book)
seedDemoData(userId)
topGenres(library)
```

Что делает:

- сохраняет книги в таблицу `books`;
- связывает пользователя и книгу через `user_books`;
- обновляет статус, оценку и отзыв;
- сохраняет дни чтения в `reading_sessions`;
- собирает статистику `LibraryStats`;
- добавляет демо-данные для демо-пользователя.

## Доменные модели

### `Book`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/Book.kt
```

Основная модель книги в приложении.

Поля:

```kotlin
localId
externalId
source
title
authors
description
categories
publishedDate
pageCount
language
previewLink
imageLink
```

Используется в UI, репозиториях и рекомендациях.

### Источники книг

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/Book.kt
```

Константы:

```kotlin
GOOGLE_BOOKS_SOURCE
OPEN_LIBRARY_SOURCE
MANUAL_SOURCE
```

Нужны, чтобы понимать происхождение книги и не смешивать внешние идентификаторы разных сервисов.

### `User`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/User.kt
```

Доменная модель авторизованного пользователя.

Содержит:

```kotlin
id
username
```

В UI не передаются хеши или соли паролей.

### `LibraryBook`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/LibraryModels.kt
```

Модель книги в личной библиотеке пользователя.

Содержит:

- `userBookId`;
- `userId`;
- `book`;
- `status`;
- `rating`;
- `review`;
- даты добавления, начала и окончания чтения.

Используется на экранах библиотеки, деталей книги и статистики.

### `ReadingDay`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/LibraryModels.kt
```

Агрегированная запись чтения за день.

Содержит:

```kotlin
date
totalMinutes
totalPages
```

Используется календарем чтения.

### `LibraryStats`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/LibraryModels.kt
```

Сводная статистика библиотеки пользователя.

Содержит:

- общее количество книг;
- счетчики по статусам;
- количество прочитанных книг;
- количество текущих книг;
- количество книг `Хочу прочитать`;
- среднюю оценку;
- суммарные минуты;
- суммарные страницы;
- дни чтения;
- топ жанров.

### `GenreStat`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/LibraryModels.kt
```

Статистика по жанру.

Поля:

```kotlin
genre
count
```

Используется в статистике и рекомендациях.

### `BookGenre`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/BookGenre.kt
```

Модель жанра для быстрых кнопок поиска.

Содержит:

```kotlin
title
query
```

Например, на экране поиска пользователь нажимает `Фэнтези`, а в API отправляется соответствующий поисковый запрос.

## Бизнес-логика

### `RecommendationEngine`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/RecommendationEngine.kt
```

Класс рекомендательной логики.

Основные методы:

```kotlin
topGenres(library, limit)
recommendationQueries(topGenres)
filterAlreadyAdded(candidates, library)
```

Логика:

- учитывает только книги со статусом `READ`;
- считает частоту жанров;
- формирует поисковые запросы вида `subject:genre`;
- если жанров нет, использует базовые темы;
- убирает из рекомендаций книги, которые уже есть в библиотеке.

### `ReadingTimer`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/ReadingTimer.kt
```

Вспомогательный объект для учета времени чтения.

Методы:

```kotlin
elapsedMinutes(startedAtMillis, finishedAtMillis)
formatMinutes(minutes)
```

Особенности:

- минимальное засчитываемое время: 1 минута;
- защищает от отрицательного времени, если системное время изменилось;
- форматирует русские формы: `1 минуту`, `2 минуты`, `5 минут`.

### `PasswordHasher`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/PasswordHasher.kt
```

Класс для безопасного хранения паролей.

Основные задачи:

- создать соль;
- посчитать хеш пароля;
- проверить введенный пароль.

Используется только в `UserRepository`.

### `DemoLibrarySeed`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/DemoLibrarySeed.kt
```

Объект с демо-данными.

Содержит:

- список демо-книг;
- статусы книг;
- оценки;
- отзывы;
- сессии чтения за последние 30 дней.

Используется при демо-входе через:

```kotlin
LibraryRepository.seedDemoData(userId)
```

### `fallbackBooksForQuery`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/FallbackBooks.kt
```

Функция локального резервного каталога.

Используется, если Google Books и Open Library не вернули книг или временно недоступны.

## Room / SQLite слой

### `BooksDatabase`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/BooksDatabase.kt
```

Главный класс Room Database.

Содержит:

```kotlin
abstract fun userDao(): UserDao
abstract fun bookDao(): BookDao
abstract fun userBookDao(): UserBookDao
abstract fun readingSessionDao(): ReadingSessionDao
```

Имя БД:

```text
bookshelf.db
```

Версия:

```text
1
```

### `UserEntity`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/Entities.kt
```

Room-сущность таблицы `users`.

Хранит:

- идентификатор пользователя;
- логин;
- email;
- хеш пароля;
- соль пароля;
- дату создания;
- дату последнего входа.

### `BookEntity`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/Entities.kt
```

Room-сущность таблицы `books`.

Хранит данные книги:

- источник;
- внешний идентификатор;
- название;
- авторы;
- описание;
- жанры;
- дату публикации;
- количество страниц;
- язык;
- ссылку на обложку;
- ссылку на предпросмотр.

Индекс `source + external_id` уникальный, чтобы не создавать дубликаты одной и той же внешней книги.

### `UserBookEntity`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/Entities.kt
```

Room-сущность таблицы `user_books`.

Связывает пользователя и книгу.

Хранит пользовательские данные:

- статус чтения;
- оценку;
- отзыв;
- дату добавления;
- дату начала чтения;
- дату завершения чтения;
- дату обновления.

### `ReadingSessionEntity`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/Entities.kt
```

Room-сущность таблицы `reading_sessions`.

Хранит отдельную сессию чтения:

- книгу пользователя;
- дату чтения;
- минуты;
- страницы;
- заметку;
- дату создания записи.

Именно эти записи используются для календаря чтения и суммарной статистики.

### `ReadingStatus`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/ReadingStatus.kt
```

Enum статуса книги.

Значения:

```text
WANT_TO_READ  -> Хочу прочитать
READING       -> Читаю
READ          -> Прочитано
PAUSED        -> Отложено
DROPPED       -> Брошено
```

### `UserDao`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/UserDao.kt
```

DAO для таблицы `users`.

Используется в `UserRepository`.

Типовые операции:

- вставка пользователя;
- поиск по логину;
- обновление пользователя.

### `BookDao`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/BookDao.kt
```

DAO для таблицы `books`.

Типовые операции:

- вставка книги;
- обновление книги;
- поиск по `source + external_id`;
- поиск по локальному `book_id`.

### `UserBookDao`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/UserBookDao.kt
```

DAO для таблицы `user_books`.

Отвечает за личную библиотеку.

Основные операции:

- вставить связь пользователь-книга;
- обновить пользовательскую книгу;
- найти книгу пользователя;
- наблюдать библиотеку пользователя;
- наблюдать конкретную книгу пользователя.

Возвращает также `LibraryBookRow`, где данные `user_books` уже объединены с `books`.

### `ReadingSessionDao`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/ReadingSessionDao.kt
```

DAO для таблицы `reading_sessions`.

Основные операции:

- добавить сессию чтения;
- удалить демо-сессии по заметке;
- наблюдать агрегированные дни чтения.

`observeReadingDays` группирует записи по дате и возвращает:

```kotlin
ReadingDayRow(readDate, totalMinutes, totalPages)
```

### `AppConverters`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/local/AppConverters.kt
```

Room TypeConverters.

Нужны для:

- хранения `List<String>` в SQLite;
- конвертации `ReadingStatus`.

## Сетевой слой

### `BookService`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/network/model/BookService.kt
```

Retrofit-интерфейс Google Books API.

Основной запрос:

```kotlin
GET volumes
```

Параметры запроса:

```text
q          - строка поиска
maxResults - лимит результатов
key        - необязательный Google Books API key
```

Ключ берется из `BuildConfig.GOOGLE_BOOKS_API_KEY`, который формируется из локального `local.properties`.

Используется в `NetworkBooksRepository`.

### `OpenLibraryService`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/network/model/OpenLibraryService.kt
```

Retrofit-интерфейс Open Library API.

Основной запрос:

```kotlin
GET search.json
```

Возвращает `OpenLibrarySearchResponse` со списком `OpenLibraryDoc`.

### Модели Google Books

Папка:

```text
app/src/main/java/com/finenkodenis/bookshelf/network/model
```

Основные модели:

- `BookShelf`;
- `Items`;
- `VolumeInfo`;
- `ImageLinks`;
- `IndustryIdentifiers`;
- `AccessInfo`;
- `ReadingModes`;
- `SaleInfo`;
- `SearchInfo`;
- `Pdf`;
- `Epub`;
- `PanelizationSummary`.

Эти классы отражают структуру JSON-ответа Google Books API.

### Модели Open Library

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/network/model/OpenLibraryService.kt
```

Основные модели:

- `OpenLibrarySearchResponse`;
- `OpenLibraryDoc`.

`NetworkBooksRepository` преобразует их в общую доменную модель `Book`.

## Вспомогательные функции

### `toSecureImageUrl`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/BookImageUrl.kt
```

Функция нормализации ссылок на обложки.

Основная задача:

- заменить `http://` на `https://`;
- не портить уже корректные `https://` ссылки.

Используется перед загрузкой обложек.

### `mainBookGenres`

Файл:

```text
app/src/main/java/com/finenkodenis/bookshelf/data/BookGenre.kt
```

Список основных жанров для быстрых кнопок поиска.

Пример:

```text
Фэнтези
Фантастика
Детективы
Романы
История
Психология
Программирование
```

## Типовые сценарии взаимодействия классов

### Поиск книги

```text
SearchScreen
    -> BooksViewModel.getBooks()
        -> BooksRepository.getBooks()
            -> BookService / OpenLibraryService
            -> fallbackBooksForQuery()
        -> BooksUiState.Success
    -> BooksGridScreen
```

### Добавление книги в библиотеку

```text
BookDetailScreen
    -> BooksViewModel.saveSelectedBook()
        -> LibraryRepository.addOrUpdateBook()
            -> BookDao.insert/update
            -> UserBookDao.insert/update
```

### Учет чтения внутри приложения

```text
ReaderScreen
    -> пользователь нажимает Назад
    -> ReaderScreen показывает диалог
    -> BooksViewModel.saveReaderSession()
        -> LibraryRepository.ensureBookForReading()
        -> LibraryRepository.addReadingSession()
            -> ReadingSessionDao.insert()
```

### Загрузка статистики

```text
StatsScreen
    <- BooksViewModel.libraryStats
        <- LibraryRepository.observeStats()
            <- UserBookDao.observeLibrary()
            <- ReadingSessionDao.observeReadingDays()
```

### Подбор рекомендаций

```text
RecommendationsScreen
    -> BooksViewModel.loadRecommendations()
        -> LibraryRepository.topGenres()
            -> RecommendationEngine.topGenres()
        -> RecommendationEngine.recommendationQueries()
        -> BooksRepository.getBooks()
        -> RecommendationEngine.filterAlreadyAdded()
```

## Тестовые классы

Папка:

```text
app/src/test/java/com/finenkodenis/bookshelf
```

Основные тесты:

- `AppConvertersTest` проверяет Room-конвертеры.
- `PasswordHasherTest` проверяет хеширование и проверку паролей.
- `RecommendationEngineTest` проверяет подбор жанров и фильтрацию уже добавленных книг.
- `FallbackBooksTest` проверяет резервный каталог.
- `BookImageUrlTest` проверяет обработку URL обложек.
- `ReadingTimerTest` проверяет учет минут чтения.
- `DemoLibrarySeedTest` проверяет состав демо-данных.

## Краткая ответственность основных классов

| Класс | Ответственность |
|---|---|
| `MainActivity` | Точка входа Activity, запуск Compose |
| `BooksApplication` | Инициализация контейнера зависимостей |
| `DefaultAppContainer` | Создание Room, Retrofit и репозиториев |
| `BooksApp` | Корневой UI и переключение экранов |
| `BooksViewModel` | Состояние приложения и координация сценариев |
| `UserRepository` | Регистрация, вход, демо-вход |
| `BooksRepository` | Интерфейс поиска книг |
| `NetworkBooksRepository` | Поиск в Google Books, Open Library и fallback |
| `LibraryRepository` | Личная библиотека, статистика, сессии чтения |
| `RecommendationEngine` | Подбор жанров и фильтрация рекомендаций |
| `BooksDatabase` | Room Database |
| `UserDao` | Доступ к пользователям |
| `BookDao` | Доступ к книгам |
| `UserBookDao` | Доступ к личной библиотеке |
| `ReadingSessionDao` | Доступ к сессиям чтения |
| `Book` | Доменная модель книги |
| `LibraryBook` | Книга в библиотеке пользователя |
| `LibraryStats` | Сводная статистика пользователя |
| `DemoLibrarySeed` | Демо-книги и демо-сессии чтения |
| `ReaderScreen` | Встроенное чтение через WebView |
| `StatsScreen` | Отображение статистики и календаря |
| `RecommendationsScreen` | Отображение подборки книг |
