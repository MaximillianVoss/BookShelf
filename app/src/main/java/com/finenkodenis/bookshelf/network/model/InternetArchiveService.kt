package com.finenkodenis.bookshelf.network.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface InternetArchiveService {
    @GET("advancedsearch.php")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("rows") rows: Int,
        @Query("page") page: Int = 1,
        @Query("output") output: String = "json",
        @Query("fl[]") fields: List<String> = INTERNET_ARCHIVE_FIELDS
    ): InternetArchiveSearchResponse
}

data class InternetArchiveSearchResponse(
    @SerializedName("response")
    val response: InternetArchiveResponse = InternetArchiveResponse()
)

data class InternetArchiveResponse(
    @SerializedName("docs")
    val docs: List<InternetArchiveDoc> = emptyList()
)

data class InternetArchiveDoc(
    @SerializedName("identifier")
    val identifier: String? = null,
    @SerializedName("title")
    val title: JsonElement? = null,
    @SerializedName("creator")
    val creator: JsonElement? = null,
    @SerializedName("description")
    val description: JsonElement? = null,
    @SerializedName("subject")
    val subject: JsonElement? = null,
    @SerializedName("date")
    val date: JsonElement? = null,
    @SerializedName("language")
    val language: JsonElement? = null
)

private val INTERNET_ARCHIVE_FIELDS = listOf(
    "identifier",
    "title",
    "creator",
    "description",
    "subject",
    "date",
    "language"
)
