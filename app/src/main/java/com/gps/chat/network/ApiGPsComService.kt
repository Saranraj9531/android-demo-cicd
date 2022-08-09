package com.gps.chat.network

import com.gps.chat.network.ApiNames.POST.ADS
import com.gps.chat.ui.forgotpassword.model.response.ForgotPasswordResponse
import com.gps.chat.network.ApiNames.POST.CHANGE_PASSWORD
import com.gps.chat.network.ApiNames.POST.CHECK_USERNAME
import com.gps.chat.network.ApiNames.POST.FORGET_PASSWORD
import com.gps.chat.network.ApiNames.POST.SEARCH_USERS
import com.gps.chat.network.ApiNames.POST.UPLOAD
import com.gps.chat.network.ApiNames.POST.USER
import com.gps.chat.ui.login.model.request.LoginRequestModel
import com.gps.chat.ui.login.model.response.LoginResponse
import com.gps.chat.network.ApiNames.POST.USER_LOGIN
import com.gps.chat.network.ApiNames.POST.USER_LOGIN_NEW_SESSION
import com.gps.chat.network.ApiNames.POST.USER_LOGOUT
import com.gps.chat.ui.changepassword.model.ChangePasswordRequest
import com.gps.chat.ui.changepassword.model.ChangePasswordResponse
import com.gps.chat.ui.forgotpassword.model.request.ForgotPasswordRequest
import com.gps.chat.ui.home.model.AdResponse
import com.gps.chat.ui.home.model.UserProfileModel
import com.gps.chat.ui.register.model.request.RegistrationRequestModel
import com.gps.chat.ui.register.model.response.RegistrationResponse
import com.gps.chat.ui.searchuser.model.SearchUserResponse
import com.gps.chat.ui.settings.model.request.UpdateProfileModel
import com.gps.chat.ui.settings.model.response.UpdateProfileResponse
import com.gps.chat.ui.welcome.model.response.CheckUserNameResponse
import com.gps.chat.utils.CommonResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 *Created by Nivetha S on 31-01-2022.
 */
interface ApiGPsComService {

    /* @GET(ApiNames.GET.GET_COUNTRY_STATE_CITY)
     suspend fun getAddressDetails(
         @Query("country") country: String,
         @Query("state") state: String,
         @Query("city") city: String
     ): Response<CountryStateCityResponse>

   */

    @POST(USER)
    suspend fun userRegister(@Body registrationRequestModel: RegistrationRequestModel): Response<RegistrationResponse>


    @POST(USER_LOGIN)
    suspend fun userLogin(@Body loginRequestModel: LoginRequestModel): Response<LoginResponse>

    @Multipart
    @POST(UPLOAD)
    suspend fun uploadImageApi(@Part media: MultipartBody.Part): Response<CommonResponse>


    @POST(USER_LOGIN_NEW_SESSION)
    suspend fun userLoginNewSession(@Body loginRequestModel: LoginRequestModel): Response<LoginResponse>

    @POST(FORGET_PASSWORD)
    suspend fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @FormUrlEncoded
    @POST(CHECK_USERNAME)
    suspend fun checkUserName(@Field("username") username: String): Response<CheckUserNameResponse>

    @POST(CHANGE_PASSWORD)
    suspend fun changePassword(
        @Body changePasswordRequest: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @PUT(USER)
    suspend fun updateProfile(@Body updateProfileModel: UpdateProfileModel): Response<UpdateProfileResponse>


    @GET("$USER/{id}")
    suspend fun getUserProfile(@Path("id") id: String): Response<UserProfileModel>

    @GET(ADS)
    suspend fun getAds(): Response<AdResponse>

    @FormUrlEncoded
    @POST(SEARCH_USERS)
    suspend fun searchUser(@Field("username") keyword: String): Response<SearchUserResponse>


    @POST(USER_LOGOUT)
    suspend fun userLogout(): Response<CommonResponse>

}