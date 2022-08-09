package com.gps.chat.ui.searchuser.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gps.chat.network.Resource
import com.gps.chat.ui.searchuser.model.SearchUserResponse
import com.gps.chat.ui.searchuser.repository.SearchUserApiRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchUserViewModel @Inject constructor(private var searchUserApiRepo: SearchUserApiRepo) :
    ViewModel() {

    private val _searchUserResponse = MutableLiveData<Resource<SearchUserResponse>>()
    val searchUserResponse: LiveData<Resource<SearchUserResponse>> = _searchUserResponse



    fun searchUser(keyword: String) = viewModelScope.launch {
        _searchUserResponse.postValue(Resource.loading())
        searchUserApiRepo.searchUser(keyword).let {
            if (it.data != null) {
                _searchUserResponse.postValue(Resource.success(it.data))
            } else {
                _searchUserResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }
        }
    }




}