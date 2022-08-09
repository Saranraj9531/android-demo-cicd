package com.gps.chat.ui.searchuser.repository

import com.gps.chat.network.ApiHelperImpl
import javax.inject.Inject

class SearchUserApiRepo @Inject constructor(private var apiHelperImpl: ApiHelperImpl) {

    suspend fun searchUser(keyword: String) =
        apiHelperImpl.searchUser(keyword)



}