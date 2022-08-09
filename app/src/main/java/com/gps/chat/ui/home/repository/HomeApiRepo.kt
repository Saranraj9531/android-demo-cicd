package com.gps.chat.ui.home.repository

import com.gps.chat.network.ApiHelperImpl
import javax.inject.Inject

class HomeApiRepo @Inject constructor(private var apiHelperImpl: ApiHelperImpl) {

    suspend fun getUserDetails(userId: String) =
        apiHelperImpl.getUserDetails(userId)


    suspend fun getAds() =
        apiHelperImpl.getAds()


}