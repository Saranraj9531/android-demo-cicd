package com.sparkout.chat.ui.addparticipants.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.BaseUtils.Companion.preventDoubleClick
import com.sparkout.chat.common.BaseUtils.Companion.snackBar
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.TUID
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMessageStatus
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.common.model.UserDetailsModel
import com.sparkout.chat.databinding.ActivityAddParticipantsBinding
import com.sparkout.chat.ui.addparticipants.model.GroupAddMember
import com.sparkout.chat.ui.addparticipants.model.SearchUserModel
import com.sparkout.chat.ui.addparticipants.model.SearchUserRequest
import com.sparkout.chat.ui.addparticipants.view.adapter.UserParticipantsAdapter
import com.sparkout.chat.ui.addparticipants.view.adapter.UserParticipantsSelectedAdapter
import com.sparkout.chat.ui.addparticipants.viewmodel.AddParticipantViewModel
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.creategroup.view.CreateGroupActivity
import com.sparkout.chat.ui.groupchat.model.MembersModel
import org.json.JSONObject
import retrofit2.Response

class AddParticipantsActivity : AppCompatActivity(), View.OnClickListener, TextWatcher {
    private var mAddParticipantBoolean: Boolean = false
    private lateinit var mAddParticipantViewModel: AddParticipantViewModel
    var mListUsers = ArrayList<UserDetailsModel>()
    lateinit var mUserParticipantsAdapter: UserParticipantsAdapter
    private lateinit var mUserParticipantsSelectedAdapter: UserParticipantsSelectedAdapter
    var mListUsersSelected = ArrayList<UserDetailsModel>()
    var mGroupId: String = ""
    var indexValue: Int = 0
    var totalCount: Int = 0
    var mSearchCharc: String = ""
    private lateinit var binding: ActivityAddParticipantsBinding

    companion object {
        lateinit var activity: Activity
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddParticipantsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activity = this

        mAddParticipantBoolean = intent.getBooleanExtra("isFromMain", false)
        if (mAddParticipantBoolean) {
            binding.textviewCreateGroup.text = resources.getString(R.string.str_add_participant)

            mGroupId = intent.getStringExtra("groupId")!!
        } else {
            binding.textviewCreateGroup.text = resources.getString(R.string.str_create_chat_group)
        }

        mAddParticipantViewModel = ViewModelProvider(this).get(AddParticipantViewModel::class.java)

        mUserParticipantsAdapter = UserParticipantsAdapter(
            this@AddParticipantsActivity,
            mListUsers,
            mListUsersSelected
        )
        binding.rvContacts.adapter = mUserParticipantsAdapter

        mUserParticipantsSelectedAdapter = UserParticipantsSelectedAdapter(this, mListUsersSelected)
        binding.rvSelectedContact.adapter = mUserParticipantsSelectedAdapter

        listenObserver()

        binding.imageviewBack.setOnClickListener(this)
        binding.textviewNext.setOnClickListener(this)
        binding.edittextSearchMember.addTextChangedListener(this)

        try {
            if (!mAddParticipantBoolean) {
                binding.rvContacts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (!binding.rvContacts.canScrollVertically(1)) {
                            if (mListUsers.size < totalCount) {
                                loadMore()
                            }
                        }
                    }
                })
                val searchUserRequest = SearchUserRequest()
                searchUserRequest.senderId = SharedPreferenceEditor.getData(
                    TUID
                )
                searchUserRequest.indexValue = 0
                searchUserRequest.keyword = ""
//                mAddParticipantViewModel.searchUser(searchUserRequest)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (mAddParticipantBoolean) {
            val mGroupMembersList =
                ChatApp.mAppDatabase!!.getGroupMemberDao()
                    .getGroupMembers(
                        mGroupId,
                        SharedPreferenceEditor.getData(USER_ID)
                    )
            val mAlreadyFriendList = ArrayList<String>()
            if (mGroupMembersList.isNotEmpty()) {
                for (i in 0 until mGroupMembersList.size) {
                    if (mGroupMembersList[i].memberId != SharedPreferenceEditor.getData(USER_ID)) {
                        mAlreadyFriendList.add(mGroupMembersList[i].memberId)
                    }
                }
            }

            mListUsers.clear()
            mListUsers.addAll(
                ChatApp.mAppDatabase!!.getUserDetailsDao()
                    .getUsersList(SharedPreferenceEditor.getData(USER_ID))
            )
            val mTempList = ArrayList<UserDetailsModel>()
            mTempList.addAll(mListUsers)
            for (i in 0 until mTempList.size) {
                for (j in 0 until mAlreadyFriendList.size) {
                    if (mTempList[i].id == mAlreadyFriendList[j] || mTempList[i].id == SharedPreferenceEditor.getData(
                            USER_ID
                        )
                    ) {
                        mListUsers.remove(mTempList[i])
                    }
                }
            }
            mUserParticipantsAdapter.notifyDataSetChanged()
        }
    }

    private fun listenObserver() {
        /* mAddParticipantViewModel.searchUserResponse()
             .observe(this, object : Observer<Response<SearchUserModel>> {
                 override fun onChanged(response: Response<SearchUserModel>?) {
                     if (response!!.isSuccessful && response.code() == 200) {
                         Log.e("Nive ", "onChanged:ParticipantsData " + response.body()?.data)
                         if (response.body()!!.status) {
                             progress_bar.visibility = View.GONE
                             totalCount = response.body()!!.totalCount
                             if (indexValue == 0) {
                                 mListUsers.clear()
                                 ChatApp.mAppDatabase!!.getUserDetailsDao().deleteFromFriendList()
                             }
                             if (response.body()!!.data.isNotEmpty()) {
                                 for (i in 0 until response.body()!!.data.size) {
                                     val userDetails = ChatApp.mAppDatabase!!.getUserDetailsDao()
                                         .getUserDetails(response.body()!!.data[i].id)
                                     if (userDetails.isEmpty()) {
                                         val userDetailsModel = response.body()!!.data[i]
                                         userDetailsModel.checkFriend = true
                                         userDetailsModel.profilePicture =
                                             "https://imagix.beebush.com/v1/ppdp/100/0/640/640/".plus(
                                                 userDetailsModel.profilePicture)
                                         ChatApp.mAppDatabase!!.getUserDetailsDao()
                                             .insert(userDetailsModel)
                                     } else {
                                         val userDetailsModel = response.body()!!.data[i]
                                         userDetailsModel.checkFriend = true
                                         userDetailsModel.profilePicture =
                                             "https://imagix.beebush.com/v1/ppdp/100/0/640/640/".plus(
                                                 userDetailsModel.profilePicture)
                                         ChatApp.mAppDatabase!!.getUserDetailsDao()
                                             .update(userDetailsModel)
                                     }
                                     mListUsers.add(response.body()!!.data[i])
                                 }
                                 mUserParticipantsAdapter.notifyDataSetChanged()
                             } else {
                                 mUserParticipantsAdapter.notifyDataSetChanged()
                             }
                         } else {
                             snackBar(this@AddParticipantsActivity,
                                      layout_parent,
                                      response.body()!!.message)
                         }
                     }*//* else if (response.code() == 421) {
                        errorAuthenticationLogout(this@SearchUserActivity)
                    }*//* else {
                        try {
                            val gson = GsonBuilder().create()
                            val searchUserResponse = gson.fromJson(response.errorBody()!!.string(),
                                                                   SearchUserModel::class.java)

                            snackBar(this@AddParticipantsActivity,
                                     layout_parent,
                                     searchUserResponse.message)
                        } catch (e: Exception) {
                        }
                    }
                }
            })
        */
    }

    /**
     * function to trigger the selected participants list on selecting a participant from a list
     * */
    fun triggerSelectedParticipants(userDetailsModel: UserDetailsModel) {
        if (mListUsersSelected.contains(userDetailsModel)) {
            mListUsersSelected.remove(userDetailsModel)
        }
        mListUsersSelected.add(userDetailsModel)
        mUserParticipantsSelectedAdapter.notifyDataSetChanged()
    }

    /**
     * function to refresh the selected participants adapter on removing a user
     * */
    fun refreshSelectedParticipants(userDetailsModel: UserDetailsModel) {
        for (i in 0 until mListUsersSelected.size) {
            if (mListUsersSelected[i].id == userDetailsModel.id) {
                mListUsersSelected.remove(mListUsersSelected[i])
                mUserParticipantsSelectedAdapter.notifyDataSetChanged()
                return
            }
        }
    }

    /**
     * function to refresh the participants adapter on removing a user from selected list
     * */
    fun refershParticipants(userDetailsModel: UserDetailsModel) {
        mListUsersSelected.remove(userDetailsModel)
        mUserParticipantsSelectedAdapter.notifyDataSetChanged()
        mUserParticipantsAdapter.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        preventDoubleClick(v!!)
        when (v.id) {
            R.id.imageview_back -> {
                onBackPressed()
            }
            R.id.textview_next -> {
                when {
                    mListUsersSelected.isEmpty() -> {
                        snackBar(
                            this,
                            binding.layoutParent,
                            resources.getString(R.string.str_minimum_member)
                        )
                    }
                    mAddParticipantBoolean -> {
                        val mGroupMembersList =
                            ChatApp.mAppDatabase!!.getGroupMemberDao()
                                .getGroupMembers(mGroupId, SharedPreferenceEditor.getData(USER_ID))
                        val mReceiversList = ArrayList<String>()
                        val mExistingGroupMemberList = ArrayList<MembersModel>()
                        var mGroupAddMemberMessage: String = "added "
                        if (mGroupMembersList.isNotEmpty()) {
                            for (i in 0 until mGroupMembersList.size) {
                                val mMembersModel = MembersModel()
                                mMembersModel.id = mGroupMembersList[i].memberId
                                mMembersModel.checkAdmin = mGroupMembersList[i].checkAdmin
                                mExistingGroupMemberList.add(mMembersModel)
                                mReceiversList.add(mGroupMembersList[i].memberId)
                            }
                        }
                        val mNewGroupMemberList = ArrayList<MembersModel>()
                        if (mListUsersSelected.isNotEmpty()) {
                            for (i in 0 until mListUsersSelected.size) {
                                val mMembersModel = MembersModel()
                                mMembersModel.id = mListUsersSelected[i].id
                                mMembersModel.checkAdmin = false
                                mNewGroupMemberList.add(mMembersModel)
                                mReceiversList.add(mListUsersSelected[i].id)
                                if (mGroupAddMemberMessage == "added ") {
                                    mGroupAddMemberMessage =
                                        mGroupAddMemberMessage.plus(mListUsersSelected[i].id)
                                } else {
                                    mGroupAddMemberMessage = mGroupAddMemberMessage.plus(",")
                                        .plus(mListUsersSelected[i].id)
                                }
                            }
                        }
                        val chatModel = ChatModel()
                        chatModel.sender = SharedPreferenceEditor.getData(USER_ID)
                        chatModel.receiver = mGroupId
                        chatModel.receivers = mReceiversList
                        chatModel.message = mGroupAddMemberMessage
                        chatModel.chatTime = BaseUtils.getUTCTime()
                        chatModel.chatType = ChatTypes.GROUP.type
                        chatModel.checkForwarded = false
                        chatModel.checkReply = false
                        chatModel.messageId =
                            BaseUtils.getMessageId(
                                BaseUtils.getUTCTime().plus("-")
                                    .plus(SharedPreferenceEditor.getData(USER_ID)).plus("-")
                                    .plus(mGroupId)
                            )
                        chatModel.messageStatus = ChatMessageStatus.SENT.ordinal
                        chatModel.messageType = ChatMessageTypes.ADDMEMBER.type
                        val mGroupAddMember = GroupAddMember()
                        mGroupAddMember.sender = SharedPreferenceEditor.getData(USER_ID)
                        mGroupAddMember.groupId = mGroupId
                        mGroupAddMember.existingMembers = mExistingGroupMemberList
                        mGroupAddMember.newMembers = mNewGroupMemberList
                        mGroupAddMember.message = chatModel
                        val mGroupAddMemberJson = Gson().toJson(mGroupAddMember)
                        val jsonObject = JSONObject(mGroupAddMemberJson)
                        ChatApp.mSocketHelper?.addGroupMember(jsonObject)
                    }
                    else -> {
                        startActivity(
                            Intent(this, CreateGroupActivity::class.java)
                                .putExtra("selectedMembers", mListUsersSelected)
                        )
                    }
                }
                finish()
            }
        }
    }

    fun loadMore() {
        indexValue++
        binding.progressBar.visibility = View.VISIBLE
        val searchUserRequest = SearchUserRequest()
        searchUserRequest.senderId = SharedPreferenceEditor.getData(
            TUID
        )
        searchUserRequest.indexValue = indexValue
        searchUserRequest.keyword = mSearchCharc
//        mAddParticipantViewModel.searchUser(searchUserRequest)
    }

    override fun afterTextChanged(s: Editable?) {
        indexValue = 0
        mSearchCharc = s.toString()
        val searchUserRequest = SearchUserRequest()
        searchUserRequest.senderId =
            SharedPreferenceEditor.getData(TUID)
        searchUserRequest.indexValue = indexValue
        searchUserRequest.keyword = mSearchCharc
//        mAddParticipantViewModel.searchUser(searchUserRequest)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}