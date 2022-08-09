package com.gps.chat.ui.settings.repository

import com.gps.chat.network.ApiHelperImpl
import com.gps.chat.ui.settings.model.request.UpdateProfileModel
import javax.inject.Inject


class UpdateProfileApiRepo @Inject constructor(var apiHelperImpl: ApiHelperImpl) {
    suspend fun updateProfile(updateProfileModel: UpdateProfileModel) =
        apiHelperImpl.updateProfile(updateProfileModel)

    suspend fun logout() =
        apiHelperImpl.logout()


}