package com.sparkout.chat.apiservice

import com.sparkout.chat.common.APINames.Companion.FRIENDLIST
import com.sparkout.chat.common.APINames.Companion.GROUP_USER_DETAIL_API
import com.sparkout.chat.common.model.AddFriendUserModel
import com.sparkout.chat.ui.addparticipants.model.SearchUserModel
import com.sparkout.chat.ui.addparticipants.model.SearchUserRequest
import org.json.JSONArray
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface BeeBushChatApiCall {
    @POST(FRIENDLIST)
    fun userList(@Body searchUserRequest: SearchUserRequest): Call<SearchUserModel>

    @POST(GROUP_USER_DETAIL_API)
    fun groupUserDetail(@Body mAddFriendUserModel: AddFriendUserModel): Call<SearchUserModel>
}