package com.gps.chat.ui.changepassword.repository

import com.gps.chat.network.ApiHelperImpl
import com.gps.chat.ui.changepassword.model.ChangePasswordRequest
import javax.inject.Inject

class ChangePasswordApiRepo @Inject constructor(private var apiHelperImpl: ApiHelperImpl) {

    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest) = apiHelperImpl.changePassword(changePasswordRequest)


}