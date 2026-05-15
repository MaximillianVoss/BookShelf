package com.finenkodenis.bookshelf.network.model

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface GutendexService {
    @GET("books/")
    suspend fun searchBooks(
        @Query("search") query: String
    ): GutendexSearchResponse
}

data class GutendexSearchResponse(
    @SerializedName("results")
    val results: List<GutendexBook> = emptyList()
)

data class GutendexBook(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("authors")
    val authors: List<GutendexPerson> = emptyList(),
    @SerializedName("subjects")
    val subjects: List<String> = emptyList(),
    @SerializedName("bookshelves")
    val bookshelves: List<String> = emptyList(),
    @SerializedName("summaries")
    val summaries: List<String> = emptyList(),
    @SerializedName("languages")
    val languages: List<String> = emptyList(),
    @SerializedName("formats")
    val formats: Map<String, String> = emptyMap()
)

data class GutendexPerson(
    @SerializedName("name")
    val name: String? = null
)
