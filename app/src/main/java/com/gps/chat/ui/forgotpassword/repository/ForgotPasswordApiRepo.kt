package com.gps.chat.ui.forgotpassword.repository

import com.gps.chat.network.ApiHelperImpl
import com.gps.chat.ui.forgotpassword.model.request.ForgotPasswordRequest
import javax.inject.Inject


class ForgotPasswordApiRepo @Inject constructor(private var apiHelperImpl: ApiHelperImpl) {

    suspend fun forgotPassword(forgotPasswordRequest: ForgotPasswordRequest) =
        apiHelperImpl.forgotPassword(forgotPasswordRequest)


}