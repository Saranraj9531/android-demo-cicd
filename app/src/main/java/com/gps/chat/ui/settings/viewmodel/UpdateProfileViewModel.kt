package com.gps.chat.ui.settings.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gps.chat.network.Resource
import com.gps.chat.ui.settings.model.request.UpdateProfileModel
import com.gps.chat.ui.settings.model.response.UpdateProfileResponse
import com.gps.chat.ui.settings.repository.UpdateProfileApiRepo
import com.gps.chat.utils.CommonResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateProfileViewModel @Inject constructor(private var updateProfileApiRepo: UpdateProfileApiRepo) :
    ViewModel() {
    private val _updateProfileResponse = MutableLiveData<Resource<UpdateProfileResponse>>()
    val updateProfileResponse: LiveData<Resource<UpdateProfileResponse>> = _updateProfileResponse


    private val _logoutResponse = MutableLiveData<Resource<CommonResponse>>()
    val logoutResponse: LiveData<Resource<CommonResponse>> = _logoutResponse


    fun updateProfile(updateProfileModel: UpdateProfileModel) = viewModelScope.launch {
        _updateProfileResponse.postValue(Resource.loading())
        updateProfileApiRepo.updateProfile(updateProfileModel).let {
            if (it.data != null) {
                _updateProfileResponse.postValue(Resource.success(it.data))
            } else {
                _updateProfileResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }
        }
    }
    fun logout() = viewModelScope.launch {
        _logoutResponse.postValue(Resource.loading())
        updateProfileApiRepo.logout().let {
            if (it.data != null) {
                _logoutResponse.postValue(Resource.success(it.data))
            } else {
                _logoutResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }
        }
    }



}