package com.finenkodenis.bookshelf.network.model

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryService {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int
    ): OpenLibrarySearchResponse
}

data class OpenLibrarySearchResponse(
    @SerializedName("docs")
    val docs: List<OpenLibraryDoc> = emptyList()
)

data class OpenLibraryDoc(
    @SerializedName("key")
    val key: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("author_name")
    val authors: List<String>? = emptyList(),
    @SerializedName("first_publish_year")
    val firstPublishYear: Int? = null,
    @SerializedName("subject")
    val subjects: List<String>? = emptyList(),
    @SerializedName("language")
    val languages: List<String>? = emptyList(),
    @SerializedName("cover_i")
    val coverId: Int? = null,
    @SerializedName("first_sentence")
    val firstSentence: List<String>? = null
) {
    val firstSentenceText: String?
        get() = firstSentence?.firstOrNull()
}
