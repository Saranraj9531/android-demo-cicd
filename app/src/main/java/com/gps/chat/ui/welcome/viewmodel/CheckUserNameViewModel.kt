package com.gps.chat.ui.welcome.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gps.chat.network.Resource
import com.gps.chat.ui.welcome.model.response.CheckUserNameResponse
import com.gps.chat.ui.welcome.repository.CheckUserNameApiRepository
import com.gps.chat.utils.isEmptyCheck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckUserNameViewModel @Inject constructor(private var checkUserNameApiRepository: CheckUserNameApiRepository) :
    ViewModel() {


    private val _checkUserNameResponse = MutableLiveData<Resource<CheckUserNameResponse>>()
    val checkUserNameResponse: LiveData<Resource<CheckUserNameResponse>> = _checkUserNameResponse

    private val validationMutableLiveData = MutableLiveData<Boolean>()
    fun isUserValid(): LiveData<Boolean> = validationMutableLiveData


    var mGender: String = ""
        set(value) {
            field = value
            loginValidations()
        }


    var mUserName: String = ""
        set(value) {
            field = value
            loginValidations()
        }


    var mDob: String = ""
        set(value) {
            field = value
            loginValidations()
        }

    private fun loginValidations() {
        var isGender = mGender.isEmptyCheck()
        var isUserName = mUserName.isEmptyCheck()
        var isDob = mDob.isEmptyCheck()

        if (isGender && isUserName && isDob) {
            validationMutableLiveData.postValue(true)
        } else {
            validationMutableLiveData.postValue(false)
        }

    }

    fun checkUserName(userName: String) = viewModelScope.launch {
        _checkUserNameResponse.postValue(Resource.loading())
        checkUserNameApiRepository.checkUserName(userName).let {
            if (it.data != null) {
                _checkUserNameResponse.postValue(Resource.success(it.data))
            } else {
                _checkUserNameResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }
        }

    }

}