package com.sparkout.chat.ui.chat.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sparkout.chat.apiservice.ApiCall
import com.sparkout.chat.apiservice.BeeBushChatApiCall
import com.sparkout.chat.apiservice.GifApiCall
import com.sparkout.chat.common.BaseUtils.Companion.GIPHY_API_KEY
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.model.AddFriendUserModel
import com.sparkout.chat.network.ApiHelperImpl
import com.sparkout.chat.ui.addparticipants.model.SearchUserModel
import com.sparkout.chat.ui.chat.model.GifResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

class ChatApiRepo @Inject constructor(private var apiHelperImpl: ApiHelperImpl) {


    suspend fun getGifData() =
        apiHelperImpl.getGifData()


    suspend fun groupUserDetail(mAddFriendUserModel: AddFriendUserModel) =
        apiHelperImpl.getUserDetails(mAddFriendUserModel)


}