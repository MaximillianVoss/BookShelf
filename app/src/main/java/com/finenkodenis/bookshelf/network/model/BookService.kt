package com.finenkodenis.bookshelf.network.model

import com.example.bookshelf.BookShelf
import retrofit2.http.GET
import retrofit2.http.Query

interface BookService {

    @GET("volumes")
    suspend fun bookSearch(
        @Query("q") searchQuery: String,
        @Query("maxResults") maxResults: Int,
        @Query("key") apiKey: String? = null
    ): BookShelf
}
