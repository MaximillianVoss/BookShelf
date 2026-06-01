# BookShelf Local Book Server

Локальный Python-сервер для ветки `local-book-server`. Он отдает Android-приложению каталог книг, обложки и HTML-страницы для встроенного чтения.

Каталог содержит 132 тестовые книги: 6 базовых классических записей и 126 сгенерированных демо-книг по основным жанрам Android-приложения.

## Запуск

```powershell
cd book_server
python -m book_server.main
```

По умолчанию сервер слушает `127.0.0.1:8000`.

Android-эмулятор обращается к нему по адресу `http://10.0.2.2:8000/`. На реальном телефоне нужно заменить базовый адрес в Android-клиенте на IP ноутбука в локальной сети.

## Виртуальное окружение

Сервер не требует внешних библиотек, но для PyCharm и воспроизводимого запуска добавлен `requirements.txt`.

Создать окружение:

```powershell
cd book_server
python -m venv venv
```

Установить зависимости:

```powershell
.\venv\Scripts\python.exe -m pip install -r requirements.txt
```

Запустить сервер через окружение:

```powershell
.\venv\Scripts\python.exe -m book_server.main
```

## API

- `GET /health` - проверка, что сервер запущен.
- `GET /api/books?q=war&limit=20` - поиск книг.
- `GET /api/books/{id}` - карточка книги.
- `GET /api/books/{id}/content` - структурированный текст книги.
- `GET /books/{id}/read` - HTML-страница для чтения во встроенном WebView.
- `GET /api/books/{id}/cover.png` - простая PNG-обложка.

## Жанры тестового набора

По 9 книг добавлено для каждого жанра:

- Fantasy
- Science Fiction
- Detective
- Romance
- Classic Literature
- Horror
- Adventure
- Psychology
- Business
- History
- Biography
- Programming
- Children
- Comics

Эти названия специально совпадают с жанровыми запросами Android-приложения, например `subject:fantasy`, `subject:science fiction`, `subject:programming`.

## Тесты

```powershell
cd book_server
python -m unittest discover -s tests
```
