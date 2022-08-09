package com.gps.chat.ui.login.viewmodel

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.installations.FirebaseInstallations
import com.gps.chat.ui.login.model.request.LoginRequestModel
import com.gps.chat.ui.login.model.response.LoginResponse
import com.gps.chat.fcm.MyFirebaseMessagingService
import com.gps.chat.network.Resource
import com.gps.chat.ui.login.repository.LoginRepository
import com.gps.chat.utils.isEmptyCheck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository) :
    ViewModel() {

    private val _loginResponse = MutableLiveData<Resource<LoginResponse>>()
    val loginResponse: LiveData<Resource<LoginResponse>> = _loginResponse

    private val validationMutableLiveData = MutableLiveData<Boolean>()
    fun isUserValid(): LiveData<Boolean> = validationMutableLiveData


    var mUserName: String = ""
        set(value) {
            field = value
            loginValidations()
        }

    var mPassword: String = ""
        set(value) {
            field = value
            loginValidations()
        }

    private fun loginValidations() {
        val isUserName = mUserName.isEmptyCheck()
        val isPassword = mPassword.isEmptyCheck()

        if (isUserName && isPassword) {
            validationMutableLiveData.postValue(true)
        } else {
            validationMutableLiveData.postValue(false)
        }

    }

    fun login() = viewModelScope.launch {
        val loginRequestModel = loginFormLambda {
            username = mUserName
            password = mPassword
            deviceType = "ANDROID"
            deviceId = Build.ID
            deviceModel = Build.MODEL

        }
        _loginResponse.postValue(Resource.loading())
        loginRepository.loginUser(loginRequestModel).let {
            if (it.data != null) {
                _loginResponse.postValue(Resource.success(it.data))
            } else {
                _loginResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }

        }
    }

    fun loginNewSession() = viewModelScope.launch {
        val loginRequestModel = loginFormLambda {
            if (MyFirebaseMessagingService.getDeviceToken().isEmpty()) {
                deviceToken = FirebaseInstallations.getInstance().id.toString()
            } else {
                deviceToken = MyFirebaseMessagingService.getDeviceToken()
            }
            deviceType = "ANDROID"
            deviceId = Build.ID
            deviceModel = Build.MODEL
        }
        _loginResponse.postValue(Resource.loading())
        loginRepository.loginUserNewSession(loginRequestModel).let {
            if (it.data != null) {
                _loginResponse.postValue(Resource.success(it.data))
            } else {
                _loginResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }

        }
    }

    private fun loginFormLambda(loginRequestModel: LoginRequestModel.() -> Unit): LoginRequestModel =
        LoginRequestModel().apply(loginRequestModel)


}