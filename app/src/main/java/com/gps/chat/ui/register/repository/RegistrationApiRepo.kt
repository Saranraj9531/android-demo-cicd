package com.gps.chat.ui.register.repository

import com.gps.chat.network.ApiHelperImpl
import com.gps.chat.ui.register.model.request.RegistrationRequestModel
import javax.inject.Inject

class RegistrationApiRepo @Inject constructor(private val apiHelperImpl: ApiHelperImpl) {

    suspend fun registerUser(registrationRequestModel: RegistrationRequestModel) = apiHelperImpl.register(registrationRequestModel)


}