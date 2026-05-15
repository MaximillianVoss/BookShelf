package com.finenkodenis.bookshelf.network.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface LibraryOfCongressService {
    @GET("books/")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("c") count: Int,
        @Query("fo") format: String = "json"
    ): LibraryOfCongressSearchResponse
}

data class LibraryOfCongressSearchResponse(
    @SerializedName("results")
    val results: List<LibraryOfCongressItem> = emptyList()
)

data class LibraryOfCongressItem(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("description")
    val description: JsonElement? = null,
    @SerializedName("contributor")
    val contributor: JsonElement? = null,
    @SerializedName("creator")
    val creator: JsonElement? = null,
    @SerializedName("subject")
    val subject: JsonElement? = null,
    @SerializedName("language")
    val language: JsonElement? = null,
    @SerializedName("image_url")
    val imageUrl: List<String> = emptyList()
)
