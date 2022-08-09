package com.sparkout.chat.network

import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.model.AddFriendUserModel
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 *Created by Nivetha S on 11-11-2021.
 */
class ApiHelperImpl @Inject constructor(
    private val apiGpsComService: ApiGPsComChatService
) : BaseDataSource() {

    suspend fun uploadImageSus(
        imageBody: MultipartBody.Part
    ) =
        getResult { apiGpsComService.uploadImageApiSus(imageBody) }

    suspend fun uploadImage(
        imageBody: MultipartBody.Part
    ) =
        getResult { apiGpsComService.uploadImageApi(imageBody) }


    suspend fun getGifData(
    ) =
        getResult { apiGpsComService.getGif(BaseUtils.GIPHY_API_KEY) }


    suspend fun getUserDetails(
        mAddFriendUserModel: AddFriendUserModel
    ) =
        getResult { apiGpsComService.groupUserDetail(mAddFriendUserModel) }


}