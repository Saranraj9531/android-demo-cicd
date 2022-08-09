package com.sparkout.chat.network

import com.sparkout.chat.common.APINames
import com.sparkout.chat.common.model.AddFriendUserModel
import com.sparkout.chat.common.model.CommonResponse
import com.sparkout.chat.ui.addparticipants.model.SearchUserModel
import com.sparkout.chat.ui.addparticipants.model.SearchUserRequest
import com.sparkout.chat.ui.chat.model.GifResponse
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


/**
 *Created by Nivetha S on 31-01-2022.
 */
interface ApiGPsComChatService {


    @Multipart
    @POST(APINames.UPLOAD)
    suspend fun uploadImageApi(@Part media: MultipartBody.Part): Response<CommonResponse>

    @Multipart
    @POST(APINames.UPLOAD)
    suspend fun uploadImageApiSus(@Part media: MultipartBody.Part): Response<CommonResponse>


    @GET("trending")
    suspend fun getGif(@Query("api_key") gif_api_key: String): Response<GifResponse>

    @POST(APINames.FRIENDLIST)
    suspend fun userList(@Body searchUserRequest: SearchUserRequest): Response<SearchUserModel>

    @POST(APINames.GROUP_USER_DETAIL_API)
    suspend fun groupUserDetail(@Body mAddFriendUserModel: AddFriendUserModel): Response<SearchUserModel>

}