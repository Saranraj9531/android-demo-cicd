package com.gps.chat.ui.changepassword.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gps.chat.network.Resource
import com.gps.chat.ui.changepassword.model.ChangePasswordRequest
import com.gps.chat.ui.changepassword.model.ChangePasswordResponse
import com.gps.chat.ui.changepassword.repository.ChangePasswordApiRepo
import com.gps.chat.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(var changePasswordApiRepo: ChangePasswordApiRepo) :
    ViewModel() {

    private val _changePasswordResponse = MutableLiveData<Resource<ChangePasswordResponse>>()
    val changePasswordResponse: LiveData<Resource<ChangePasswordResponse>> = _changePasswordResponse

    private val validationMutableLiveData = MutableLiveData<Boolean>()
    fun isUserValid(): LiveData<Boolean> = validationMutableLiveData


    var mOldPassword: String = ""
        set(value) {
            field = value
            changeOldPasswordValidations()
        }

    private fun changeOldPasswordValidations() {
        if (mOldPassword.isEmptyCheck()) {
            showToast(App.mInstance.applicationContext, "Enter your  password")
        } else if (mOldPassword.passwordValidation()) {
            showToast(App.mInstance.applicationContext, "Password must be 15 character minimum")
        }
    }

    var mNewPassword: String = ""
        set(value) {
            field = value
            checkNewPassword()
        }

    private fun checkNewPassword() {
        if (mNewPassword.isEmptyCheck()) {
            showToast(App.mInstance.applicationContext, "Enter your new password")
        } else if (!mNewPassword.passwordValidation()) {
            showToast(App.mInstance.applicationContext, "Password must be 15 character minimum")
        }
    }

    private fun checkConfirmPassword() {
        if (mConfirmPassword.isEmptyCheck()) {
            showToast(App.mInstance.applicationContext, "Enter your password again to confirm")
        } else if (!mConfirmPassword.passwordValidation()) {
            showToast(App.mInstance.applicationContext, "Password must be 15 character minimum")
        } else if (!passwordCompareValidation(mConfirmPassword, mNewPassword)) {
            showToast(
                App.mInstance.applicationContext,
                "Passwords do not match. Please check your inputs"
            )

        } else {
            checkValidations()

        }
    }

    private fun checkValidations() {
        if (mOldPassword.isEmptyCheck() && mNewPassword.isEmptyCheck() && mConfirmPassword.isEmptyCheck()) {
            validationMutableLiveData.postValue(true)
        } else {
            validationMutableLiveData.postValue(false)

        }
    }

    var mConfirmPassword: String = ""
        set(value) {
            field = value
            checkConfirmPassword()
        }


    fun changePassword(changePasswordRequest: ChangePasswordRequest) = viewModelScope.launch {
        _changePasswordResponse.postValue(Resource.loading())
        changePasswordApiRepo.changePassword(changePasswordRequest).let {
            if (it.data != null) {
                _changePasswordResponse.postValue(Resource.success(it.data))
            } else {
                _changePasswordResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }


        }

    }

    private fun changePasswordFormLambda(changePasswordRequestModel: ChangePasswordRequest.() -> Unit): ChangePasswordRequest =
        ChangePasswordRequest().apply(changePasswordRequestModel)


}