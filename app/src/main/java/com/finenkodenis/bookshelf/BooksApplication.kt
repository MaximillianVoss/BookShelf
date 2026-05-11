package com.finenkodenis.bookshelf

import android.app.Application
import com.finenkodenis.bookshelf.data.AppContainer
import com.finenkodenis.bookshelf.data.DefaultAppContainer

class BooksApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
