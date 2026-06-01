package com.finenkodenis.bookshelf.network.model

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface LocalBookServerService {
    @GET("api/books")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int
    ): LocalBookServerSearchResponse
}

data class LocalBookServerSearchResponse(
    @SerializedName("items")
    val items: List<LocalBookServerBook> = emptyList()
)

data class LocalBookServerBook(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("authors")
    val authors: List<String> = emptyList(),
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("categories")
    val categories: List<String> = emptyList(),
    @SerializedName("publishedDate")
    val publishedDate: String? = null,
    @SerializedName("pageCount")
    val pageCount: Int? = null,
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("readerUrl")
    val readerUrl: String? = null,
    @SerializedName("coverUrl")
    val coverUrl: String? = null
)
