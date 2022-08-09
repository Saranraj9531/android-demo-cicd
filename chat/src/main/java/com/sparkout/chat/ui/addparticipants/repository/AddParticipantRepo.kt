package com.sparkout.chat.ui.addparticipants.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sparkout.chat.apiservice.BeeBushChatApiCall
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.ui.addparticipants.model.SearchUserModel
import com.sparkout.chat.ui.addparticipants.model.SearchUserRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

class AddParticipantRepo {
    val searchUserModel = MutableLiveData<Response<SearchUserModel>>()
    val apiError = MutableLiveData<String>()


    /*fun getUserDetails(context: Context): LiveData<List<UserDetailsModel>> {
        mListUserDetails.postValue(mAppDatabase!!.getUserDetailsDao().getUserDetailsList(getUserId(context)!!))
        return mListUserDetails
    }*/
    fun searchUser(searchUserRequest: SearchUserRequest): LiveData<Response<SearchUserModel>> {
        /*val call = apiInterface.userList(searchUserRequest)
        call.enqueue(object : Callback<SearchUserModel> {
            override fun onFailure(call: Call<SearchUserModel>, t: Throwable) {
                apiError.postValue("Internal Server Error")
            }

            override fun onResponse(call: Call<SearchUserModel>,
                                    response: Response<SearchUserModel>) {
                searchUserModel.postValue(response)
            }
        })
        return searchUserModel*/
        return searchUserModel
    }
    /*fun getFriendList(mAlreadyFriendsList: ArrayList<String>): LiveData<List<UserDetailsModel>> {
        mListUserDetails.postValue(mAppDatabase!!.getUserDetailsDao()
                                       .getFriendList(mAlreadyFriendsList))
        return mListUserDetails
    }*/
}