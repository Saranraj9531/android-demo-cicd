package com.sparkout.chat.ui.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.model.AddFriendUserModel
import com.sparkout.chat.network.Resource
import com.sparkout.chat.ui.addparticipants.model.SearchUserModel
import com.sparkout.chat.ui.chat.model.GifResponse
import com.sparkout.chat.ui.chat.repository.ChatApiRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private var chatApiRepo: ChatApiRepo) : ViewModel() {

    private val _gifResponse = MutableLiveData<Resource<GifResponse>>()
    val gifResponse: LiveData<Resource<GifResponse>> = _gifResponse

    private val _userDetailsResponse = MutableLiveData<Resource<SearchUserModel>>()
    val userDetailsResponse: LiveData<Resource<SearchUserModel>> = _userDetailsResponse

    fun getGifData() = viewModelScope.launch {
        _gifResponse.postValue(Resource.loading())
        chatApiRepo.getGifData().let {
            if (it.data != null) {
                _gifResponse.postValue(Resource.success(it.data))
            } else {
                _gifResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }
        }

    }

    fun getGroupUserDetails(mAddFriendUserModel: AddFriendUserModel) = viewModelScope.launch {
        _userDetailsResponse.postValue(Resource.loading())
        chatApiRepo.groupUserDetail(mAddFriendUserModel).let {
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


}