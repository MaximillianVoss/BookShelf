package com.finenkodenis.bookshelf.ui.theme.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.finenkodenis.bookshelf.data.ReadingTimer

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderScreen(
    title: String,
    url: String?,
    elapsedMinutes: () -> Int,
    onBack: () -> Unit,
    onSaveReadingSession: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (url.isNullOrBlank()) {
        EmptyState(
            title = "Чтение недоступно",
            subtitle = "Для этой книги источник не предоставил ссылку для просмотра.",
            modifier = modifier
        )
        return
    }

    var showExitDialog by remember(url) { mutableStateOf(false) }
    var minutesToSave by remember(url) { mutableStateOf(1) }
    fun requestExit() {
        minutesToSave = elapsedMinutes()
        showExitDialog = true
    }

    BackHandler(onBack = ::requestExit)

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Засчитать чтение?") },
            text = {
                Text(
                    "Сохранить ${ReadingTimer.formatMinutes(minutesToSave)} чтения в статистику? " +
                        "Если книги еще нет в библиотеке, она будет добавлена со статусом \"Читаю\"."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onSaveReadingSession(minutesToSave)
                    }
                ) {
                    Text("Засчитать")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onBack()
                    }
                ) {
                    Text("Не засчитывать")
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OutlinedButton(onClick = ::requestExit) {
                Text("Назад")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title.ifBlank { "Чтение" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url) {
                    webView.loadUrl(url)
                }
            }
        )
    }
}
