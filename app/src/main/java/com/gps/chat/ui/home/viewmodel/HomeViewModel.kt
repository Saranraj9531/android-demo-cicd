package com.gps.chat.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gps.chat.network.Resource
import com.gps.chat.ui.home.model.AdResponse
import com.gps.chat.ui.home.model.UserProfileModel
import com.gps.chat.ui.home.repository.HomeApiRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private var homeApiRepo: HomeApiRepo) : ViewModel() {

    private val _userDetailsResponse = MutableLiveData<Resource<UserProfileModel>>()
    val userDetailsResponse: LiveData<Resource<UserProfileModel>> = _userDetailsResponse

    private val _adsResponse = MutableLiveData<Resource<AdResponse>>()
    val adsResponse: LiveData<Resource<AdResponse>> = _adsResponse


    fun getUserDetails(userId: String) = viewModelScope.launch {
        _userDetailsResponse.postValue(Resource.loading())
        homeApiRepo.getUserDetails(userId).let {
            if (it.data != null) {
                _userDetailsResponse.postValue(Resource.success(it.data))
            } else {
                _userDetailsResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }
        }

    }

    fun getAds() = viewModelScope.launch {
        _adsResponse.postValue(Resource.loading())
        homeApiRepo.getAds().let {
            if (it.data != null) {
                _adsResponse.postValue(Resource.success(it.data))
            } else {
                _adsResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }
        }


    }


}