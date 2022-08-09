package com.gps.chat.ui.register.viewmodel

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gps.chat.fcm.MyFirebaseMessagingService
import com.gps.chat.network.Resource
import com.gps.chat.ui.register.model.request.RegistrationRequestModel
import com.gps.chat.ui.register.model.response.RegistrationResponse
import com.gps.chat.ui.register.repository.RegistrationApiRepo
import com.gps.chat.utils.isEmptyCheck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(private val registrationApiRepo: RegistrationApiRepo) :
    ViewModel() {

    private val _registerResponse = MutableLiveData<Resource<RegistrationResponse>>()
    val registerResponse: LiveData<Resource<RegistrationResponse>> = _registerResponse

    private val validationMutableLiveData = MutableLiveData<Boolean>()
    fun isUserValid(): LiveData<Boolean> = validationMutableLiveData

    var mUserName: String = ""
        set(value) {
            field = value
            registerValidations()
        }

    var mGender: String = ""
        set(value) {
            field = value
            registerValidations()
        }

    var mDateOfBirth: String = ""
        set(value) {
            field = value
            registerValidations()
        }

    var mProfilePicture: String = ""
        set(value) {
            field = value
            registerValidations()
        }
    var mName: String = ""
        set(value) {
            field = value
            registerValidations()
        }
    var mPassportNumber: String = ""
        set(value) {
            field = value
            registerValidations()
        }
    var mPassportImage: String = ""
        set(value) {
            field = value
            registerValidations()
        }


    private fun registerValidations() {
        var isUserName = mUserName.isEmptyCheck()
        var isGender = mGender.isEmptyCheck()
        var isDateOfBirth = mDateOfBirth.isEmptyCheck()
        var isProfilePicture = mProfilePicture.isEmptyCheck()
        var isName = mName.isEmptyCheck()
        var isPassportNumber = mPassportNumber.isEmptyCheck()
        var isPassportImage = mPassportImage.isEmptyCheck()

        if (isUserName && isGender && isDateOfBirth && isProfilePicture && isName && isPassportNumber && isPassportImage) {
            validationMutableLiveData.postValue(true)
        } else {
            validationMutableLiveData.postValue(false)
        }

    }


    fun registerUser() = viewModelScope.launch {
        val registerRequest = registerFormLambda {
            username = mUserName
            gender = mGender
            dateOfBirth = mDateOfBirth
            profilePicture = mProfilePicture
            name = mName
            passportNumber = mPassportNumber
            passportImage = mPassportImage
            deviceToken = MyFirebaseMessagingService.getDeviceToken().ifEmpty {
                "NO_TOKEN"
            }
            deviceType = "ANDROID"
            deviceId = Build.ID
            deviceModel = Build.MODEL
        }
        _registerResponse.postValue(Resource.loading())
        registrationApiRepo.registerUser(registerRequest).let {
            if (it.data != null) {
                _registerResponse.postValue(Resource.success(it.data))
            } else {
                _registerResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }
        }

    }

    private fun registerFormLambda(registrationRequestModel: RegistrationRequestModel.() -> Unit): RegistrationRequestModel =
        RegistrationRequestModel().apply(registrationRequestModel)


}