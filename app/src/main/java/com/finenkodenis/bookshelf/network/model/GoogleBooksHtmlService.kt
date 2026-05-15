package com.finenkodenis.bookshelf.network.model

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface GoogleBooksHtmlService {
    @Headers(
        "User-Agent: Mozilla/5.0 (Android; BookShelf demo)",
        "Accept-Language: ru-RU,ru;q=0.9,en;q=0.8"
    )
    @GET("books")
    suspend fun searchBooksHtml(
        @Query("q") query: String,
        @Query("jscmd") command: String = "SearchWithinVolume2",
        @Query("hl") language: String = "ru"
    ): Response<ResponseBody>
}
