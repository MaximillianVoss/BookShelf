package com.finenkodenis.bookshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.finenkodenis.bookshelf.ui.theme.BookShelfTheme
import com.finenkodenis.bookshelf.ui.theme.BooksApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookShelfTheme {
                BooksApp()
            }
        }
    }
}

