package com.sparkout.chat.ui.groupinfo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.ui.groupchat.model.GroupDetailsModel
import com.sparkout.chat.ui.groupchat.model.GroupMemberModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class GroupInfoViewModel : ViewModel() {
    val mListGroupDetails = MutableLiveData<List<GroupDetailsModel>>()
    val mListGroupMembers = MutableLiveData<List<GroupMemberModel>>()


    fun getGroupDetails(mToId: String) {
        mListGroupDetails.postValue(
            ChatApp.mAppDatabase!!.getGroupDetailsDao()
                .getGroupDetails(
                    mToId,
                    SharedPreferenceEditor.getData(Global.USER_ID)
                )
        )
    }


    fun getGroupMembers(mToId: String) {
        mListGroupMembers.postValue(
            ChatApp.mAppDatabase!!.getGroupMemberDao()
                .getGroupMembers(
                    mToId,
                    SharedPreferenceEditor.getData(Global.USER_ID)
                )
        )
    }

}