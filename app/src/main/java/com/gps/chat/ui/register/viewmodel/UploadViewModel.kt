package com.gps.chat.ui.register.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gps.chat.network.Resource
import com.gps.chat.ui.register.repository.UploadApiRepository
import com.gps.chat.utils.CommonResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(var uploadApiRepository: UploadApiRepository) :
    ViewModel() {

    private val _uploadResponse = MutableLiveData<Resource<CommonResponse>>()
    val uploadResponse: LiveData<Resource<CommonResponse>> = _uploadResponse


    fun uploadImage(imageBody: MultipartBody.Part) = viewModelScope.launch {
        _uploadResponse.postValue(Resource.loading())
        uploadApiRepository.uploadImage(imageBody).let {
            if (it.data != null) {
                _uploadResponse.postValue(Resource.success(it.data))
            } else {
                _uploadResponse.postValue(it.message?.let { it1 ->
                    Resource.error(
                        it1, it.responseCode
                    )
                })
            }
        }

    }

}