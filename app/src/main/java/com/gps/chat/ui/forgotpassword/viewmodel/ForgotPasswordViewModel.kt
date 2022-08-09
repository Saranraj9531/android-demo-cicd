package com.gps.chat.ui.forgotpassword.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gps.chat.ui.forgotpassword.model.response.ForgotPasswordResponse
import com.gps.chat.network.Resource
import com.gps.chat.ui.forgotpassword.model.request.ForgotPasswordRequest
import com.gps.chat.ui.forgotpassword.repository.ForgotPasswordApiRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(private var forgotPasswordApiRepo: ForgotPasswordApiRepo) :
    ViewModel() {
    private val _forgotPasswordResponse = MutableLiveData<Resource<ForgotPasswordResponse>>()
    val forgotPasswordResponse: LiveData<Resource<ForgotPasswordResponse>> = _forgotPasswordResponse

    private val validationMutableLiveData = MutableLiveData<Boolean>()
    fun isUserValid(): LiveData<Boolean> = validationMutableLiveData

    fun forgotPassword(forgotPasswordRequest: ForgotPasswordRequest) = viewModelScope.launch {
        _forgotPasswordResponse.postValue(Resource.loading())
        forgotPasswordApiRepo.forgotPassword(forgotPasswordRequest).let {
            if (it.data != null) {
                _forgotPasswordResponse.postValue(Resource.success(it.data))
            } else {
                _forgotPasswordResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }

        }
    }


}