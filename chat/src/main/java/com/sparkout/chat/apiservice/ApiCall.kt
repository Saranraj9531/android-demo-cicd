package com.sparkout.chat.apiservice

import com.sparkout.chat.common.APINames.Companion.UPLOAD
import com.sparkout.chat.common.model.CommonResponse
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiCall {
    @Multipart
    @POST(UPLOAD)
    suspend fun uploadImageApi(@Part media: MultipartBody.Part): Call<CommonResponse>

    @Multipart
    @POST(UPLOAD)
    suspend fun uploadImageApiSus(@Part media: MultipartBody.Part): Deferred<Response<CommonResponse>>
}