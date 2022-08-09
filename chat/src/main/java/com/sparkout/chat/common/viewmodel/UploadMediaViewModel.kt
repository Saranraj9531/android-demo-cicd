package com.sparkout.chat.common.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.model.CommonResponse
import com.sparkout.chat.common.repository.UploadMediaApiRepo
import com.sparkout.chat.network.Resource
import com.sparkout.chat.ui.apisample.LiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class UploadMediaViewModel @Inject constructor(var uploadMediaApiRepo: UploadMediaApiRepo) :
    ViewModel() {


    private val _commonResponse = MutableLiveData<Resource<CommonResponse>>()
    val commonResponse: LiveData<Resource<CommonResponse>> = _commonResponse

    fun uploadImage(imageBody: MultipartBody.Part) = viewModelScope.launch {
        _commonResponse.postValue(Resource.loading())
        uploadMediaApiRepo.uploadImage(imageBody).let {
            if (it.data != null) {
                _commonResponse.postValue(Resource.success(it.data))
            } else {
                _commonResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }
        }
    }


}