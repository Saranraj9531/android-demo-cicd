package com.gps.chat.ui.register.repository

import com.gps.chat.network.ApiHelperImpl
import okhttp3.MultipartBody
import javax.inject.Inject

class UploadApiRepository @Inject constructor(var apiHelperImpl: ApiHelperImpl) {
    suspend fun uploadImage(imageBody: MultipartBody.Part) = apiHelperImpl.uploadImage(imageBody)
}