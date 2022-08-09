package com.gps.chat.network

import com.gps.chat.ui.changepassword.model.ChangePasswordRequest
import com.gps.chat.ui.forgotpassword.model.request.ForgotPasswordRequest
import com.gps.chat.ui.login.model.request.LoginRequestModel
import com.gps.chat.ui.register.model.request.RegistrationRequestModel
import com.gps.chat.ui.settings.model.request.UpdateProfileModel
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 *Created by Nivetha S on 11-11-2021.
 */
class ApiHelperImpl @Inject constructor(
    private val apiGpsComService: ApiGPsComService
) : com.gps.chat.network.BaseDataSource() {

    suspend fun login(loginRequest: LoginRequestModel) =
        getResultApp { apiGpsComService.userLogin(loginRequest) }

    suspend fun loginNewSession(loginRequest: LoginRequestModel) =
        getResultApp { apiGpsComService.userLoginNewSession(loginRequest) }


    suspend fun register(registerRequestModel: RegistrationRequestModel) =
        getResultApp { apiGpsComService.userRegister(registerRequestModel) }

    suspend fun uploadImage(imageBody: MultipartBody.Part) =
        getResultApp { apiGpsComService.uploadImageApi(imageBody) }

    suspend fun checkUserName(userName: String) =
        getResultApp { apiGpsComService.checkUserName(userName) }

    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest) =
        getResultApp {
            apiGpsComService.changePassword(
                changePasswordRequest
            )
        }

    suspend fun forgotPassword(forgotPasswordRequest: ForgotPasswordRequest) =
        getResultApp { apiGpsComService.forgotPassword(forgotPasswordRequest) }


    suspend fun searchUser(userName: String) =
        getResultApp { apiGpsComService.searchUser(userName) }

    suspend fun updateProfile(updateProfileModel: UpdateProfileModel) =
        getResultApp { apiGpsComService.updateProfile(updateProfileModel) }

    suspend fun logout() =
        getResultApp { apiGpsComService.userLogout() }

    suspend fun getUserDetails(userId: String) =
        getResultApp { apiGpsComService.getUserProfile(userId) }


    suspend fun getAds() =
        getResultApp { apiGpsComService.getAds() }

}