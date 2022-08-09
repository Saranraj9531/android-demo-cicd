package com.gps.chat.ui.login.repository

import com.gps.chat.ui.login.model.request.LoginRequestModel
import com.gps.chat.network.ApiHelperImpl
import javax.inject.Inject

class LoginRepository @Inject constructor(private val apiHelper: ApiHelperImpl) {
    suspend fun loginUser(loginRequest: LoginRequestModel) = apiHelper.login(loginRequest)

    suspend fun loginUserNewSession(loginRequest: LoginRequestModel) =
        apiHelper.loginNewSession(loginRequest)


}