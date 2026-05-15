package com.finenkodenis.bookshelf.network.model

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface YandexBooksHtmlService {
    @Headers(
        "User-Agent: Mozilla/5.0 (Android; BookShelf demo)",
        "Accept-Language: ru-RU,ru;q=0.9,en;q=0.8"
    )
    @GET("search/all/{query}")
    suspend fun searchBooksHtml(
        @Path(value = "query", encoded = true) encodedQuery: String
    ): Response<ResponseBody>
}
