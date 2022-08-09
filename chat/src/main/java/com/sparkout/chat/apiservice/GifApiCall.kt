package com.sparkout.chat.apiservice

import com.sparkout.chat.ui.chat.model.GifResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GifApiCall {
    @GET("trending")
    fun getGif(@Query("api_key") gif_api_key: String): Call<GifResponse>
}