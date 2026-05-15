package com.finenkodenis.bookshelf.network.model

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HtmlBookSearchService {
    @GET("search")
    suspend fun searchBooksHtml(
        @Query("q") query: String,
        @Query("layout") layout: String = "details"
    ): Response<ResponseBody>
}
