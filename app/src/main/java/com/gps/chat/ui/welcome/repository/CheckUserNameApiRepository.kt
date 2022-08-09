package com.gps.chat.ui.welcome.repository

import com.gps.chat.network.ApiHelperImpl
import javax.inject.Inject


class CheckUserNameApiRepository @Inject constructor(private var apiHelperImpl: ApiHelperImpl) {
    suspend fun checkUserName(userName: String) = apiHelperImpl.checkUserName(userName)

}