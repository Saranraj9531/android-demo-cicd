package com.sparkout.chat.socket

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sparkout.chat.BuildConfig
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.BaseUtils.Companion.fileExist
import com.sparkout.chat.common.BaseUtils.Companion.getInfoMessageTypes
import com.sparkout.chat.common.BaseUtils.Companion.getLastMessage
import com.sparkout.chat.common.BaseUtils.Companion.getMessageId
import com.sparkout.chat.common.BaseUtils.Companion.getObjectId
import com.sparkout.chat.common.BaseUtils.Companion.getUTCTime
import com.sparkout.chat.common.BaseUtils.Companion.infoMessageTypes
import com.sparkout.chat.common.BaseUtils.Companion.sDocumentDirectoryPath
import com.sparkout.chat.common.BaseUtils.Companion.showToast
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMessageStatus
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.common.model.*
import com.sparkout.chat.common.workmanager.DownloadWorker
import com.sparkout.chat.socket.EventMethods.Companion.ADDED_IN_NEW_GROUP_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.ADD_CONTACT
import com.sparkout.chat.socket.EventMethods.Companion.AUDIO_PLAYED_ON_CHAT_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.AUDIO_PLAYED_ON_GROUP_CHAT_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.BLOCK_CONTACT_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.CREATE_GROUP_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.DELETE_FOR_EVERYONE_CHAT_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.DELETE_FOR_EVERYONE_GROUP_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.ERROR_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GET_BLOCKED_CONTACTS_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GET_GROUPS_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GET_GROUP_INFO_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GET_GROUP_MEMBERS_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GET_MY_CONTACTS
import com.sparkout.chat.socket.EventMethods.Companion.GET_USERS_INFO_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GROUP_ADD_ADMIN_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GROUP_ADD_MEMBER_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GROUP_CHAT_DELIVERED_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GROUP_CHAT_MESSAGE_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GROUP_CHAT_READ_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GROUP_EXIT_MEMBER_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GROUP_INFO_UPDATE_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GROUP_REMOVE_ADMIN_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.GROUP_REMOVE_MEMBER_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.MESSAGE_DELETED_ON_CHAT_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.MESSAGE_DELETED_ON_GROUP_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.ONLINE_OFFLINE
import com.sparkout.chat.socket.EventMethods.Companion.PROFILE_UPDATED
import com.sparkout.chat.socket.EventMethods.Companion.REPORT_MESSAGE_EVENT
import com.sparkout.chat.socket.EventMethods.Companion.SINGLE_CHAT_DELIVERED
import com.sparkout.chat.socket.EventMethods.Companion.SINGLE_CHAT_READ
import com.sparkout.chat.socket.EventMethods.Companion.SINGLE_CHAT_SEND_MESSAGE
import com.sparkout.chat.socket.EventMethods.Companion.SINGLE_CHAT_TYPING
import com.sparkout.chat.socket.EventMethods.Companion.UN_BLOCK_CONTACT_EVENT
import com.sparkout.chat.ui.addparticipants.model.GroupAddMember
import com.sparkout.chat.ui.chat.model.*
import com.sparkout.chat.ui.chat.view.ChatActivity
import com.sparkout.chat.ui.groupchat.model.*
import com.sparkout.chat.ui.groupchat.view.GroupChatActivity
import com.sparkout.chat.ui.groupinfo.model.AddOrRemoveAdminModel
import com.sparkout.chat.ui.groupinfo.model.GroupChatExitModel
import com.sparkout.chat.ui.groupinfo.model.GroupInfoModel
import com.sparkout.chat.ui.groupinfo.model.GroupRemoveMember
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.Polling
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.net.URISyntaxException
import java.text.SimpleDateFormat


class SocketHelper(context: Context) {

    private lateinit var file: File
    val onConnect = Emitter.Listener {
        Timber.tag("Krish").e("onConnect: ${mSocket.connected()}")
        if (mSocket.connected()) {
            Log.e("Nive ", ": userId connected ${SharedPreferenceEditor.getData(USER_ID)}")
            val listOfflineMessages =
                mAppDatabase!!.getChatDao()
                    .getOfflineMessages(SharedPreferenceEditor.getData(USER_ID))
            // check chat types send offline messages to cloud
            if (listOfflineMessages.isNotEmpty()) {
                for (i in listOfflineMessages.indices) {
                    if (listOfflineMessages[i].messageType == ChatMessageTypes.IMAGE.type) {
                        if (null != BaseUtils.pickImageFromStorage(
                                context,
                                listOfflineMessages[i].messageId,
                                1
                            )
                        ) {
                            file =
                                File(
                                    BaseUtils.pickImageFromStorage(
                                        context,
                                        listOfflineMessages[i].messageId,
                                        1
                                    )!!
                                )
                            if (file.exists()) {
                                EventBus.getDefault().post(
                                    UploadFileModel(
                                        file.absolutePath,
                                        ChatMessageTypes.IMAGE.type
                                    )
                                )
                            }
                        }
                    } else if (listOfflineMessages[i].messageType == ChatMessageTypes.AUDIO.type) {
                        if (null != BaseUtils.pickImageFromStorage(
                                context,
                                listOfflineMessages[i].messageId,
                                3
                            )
                        ) {
                            file =
                                File(
                                    BaseUtils.pickImageFromStorage(
                                        context,
                                        listOfflineMessages[i].messageId,
                                        3
                                    )!!
                                )
                            if (file.exists()) {
                                EventBus.getDefault().post(
                                    UploadFileModel(
                                        file.absolutePath,
                                        ChatMessageTypes.AUDIO.type
                                    )
                                )
                            }
                        }
                    } else if (listOfflineMessages[i].messageType == ChatMessageTypes.VIDEO.type) {
                        if (null != BaseUtils.pickImageFromStorage(
                                context,
                                listOfflineMessages[i].messageId,
                                2
                            )
                        ) {
                            file =
                                File(
                                    BaseUtils.pickImageFromStorage(
                                        context,
                                        listOfflineMessages[i].messageId,
                                        2
                                    )!!
                                )
                            if (file.exists()) {
                                EventBus.getDefault().post(
                                    UploadFileModel(
                                        file.absolutePath,
                                        ChatMessageTypes.VIDEO.type
                                    )
                                )
                            }
                        }
                    } else {
                        /* val checkFriendExists =
                             mAppDatabase!!.getFriendDetailsDao().checkFriendAlreadyExists(
                                 listOfflineMessages[i].receiver,
                                 SharedPreferenceEditor.getData(USER_ID))
                         if (checkFriendExists.isEmpty()) {
                             val friendDetailsModel = FriendDetailsModel()
                             friendDetailsModel.sender = SharedPreferenceEditor.getData(USER_ID)
                             friendDetailsModel.receiver = listOfflineMessages[i].receiver
                             val friendDetailsJson = Gson().toJson(friendDetailsModel)
                             val jsonObjectFriend = JSONObject(friendDetailsJson)
                             if (isOnline(context)) {
                                 ChatApp.mSocketHelper?.sendAddContact(jsonObjectFriend)
                             }
                         }*/
                        val chatJson = Gson().toJson(listOfflineMessages[i])
                        val jsonObject = JSONObject(chatJson)
                        ChatApp.mSocketHelper?.sendSingleChat(jsonObject)
                    }
                }
            }
            /*if (listOfflineMessages.isNotEmpty()) {
                for (i in listOfflineMessages.indices) {
                    val checkFriendExists =
                        mAppDatabase!!.getFriendDetailsDao().checkFriendAlreadyExists(
                            listOfflineMessages[i].receiver,
                            SharedPreferenceEditor.getData(USER_ID))
                    if (checkFriendExists.isEmpty()) {
                        val friendDetailsModel = FriendDetailsModel()
                        friendDetailsModel.sender = SharedPreferenceEditor.getData(USER_ID)
                        friendDetailsModel.receiver = listOfflineMessages[i].receiver
                        val friendDetailsJson = Gson().toJson(friendDetailsModel)
                        val jsonObjectFriend = JSONObject(friendDetailsJson)
                        if (isOnline(context)) {
                            ChatApp.mSocketHelper?.sendAddContact(jsonObjectFriend)
                        }
                    }
                    val chatJson = Gson().toJson(listOfflineMessages[i])
                    Log.e("Nive ", "onConnect:Before Send Socket ${chatJson}")
                    val jsonObject = JSONObject(chatJson)
                    ChatApp.mSocketHelper?.sendSingleChat(jsonObject)
                }
            }*/
            onlineOfflineSocket(context)
            getFriendlist(context)
            getGroups(context)
            getBlockedContactsJson(context)
        }
    }
    val onDisconnect = Emitter.Listener {
        Timber.tag("Krish").e("disconnected: ")
        EventBus.getDefault().post("DisConnect")
    }
    val onConnectError = Emitter.Listener {
        Timber.tag("Krish").e("Error connecting: ${Gson().toJson(it)}")
    }
    val onConnectTimeout = Emitter.Listener {
        Timber.tag("Krish").e("Error connecting Timeout: ")
    }

    @SuppressLint("SimpleDateFormat")
    private val mSingleChatSendMessageEvent = Emitter.Listener {
        val chatModel = GsonBuilder().create().fromJson(it[0].toString(), ChatModel::class.java)
        Timber.tag("Krish").e("mSingleChatSendMessageEvent: ${Gson().toJson(chatModel)}")
        val listReceivedChat =
            mAppDatabase!!.getChatDao()
                .getChatData(chatModel.messageId, SharedPreferenceEditor.getData(USER_ID))
        if (listReceivedChat.isNotEmpty()) {
            if (chatModel.messageStatus == ChatMessageStatus.NOT_SENT.ordinal) {
                chatModel.messageStatus = ChatMessageStatus.SENT.ordinal
            } else {
                chatModel.messageStatus = chatModel.messageStatus
            }
            if (chatModel.messageType == ChatMessageTypes.AUDIO.type) {
                val listAudioMessage =
                    mAppDatabase!!.getAudioStatusDao().getAudioMessage(
                        chatModel.messageId,
                        SharedPreferenceEditor.getData(USER_ID)
                    )
                if (listAudioMessage.isNotEmpty()) {
                    val audioStatus = AudioStatus()
                    audioStatus.userId = SharedPreferenceEditor.getData(USER_ID)
                    audioStatus.messageId = chatModel.messageId
                    audioStatus.checkPlayed = false
                    mAppDatabase!!.getAudioStatusDao().update(audioStatus)
                } else {
                    val audioStatus = AudioStatus()
                    audioStatus.userId = SharedPreferenceEditor.getData(USER_ID)
                    audioStatus.messageId = chatModel.messageId
                    audioStatus.checkPlayed = false
                    mAppDatabase!!.getAudioStatusDao().insert(audioStatus)
                }
            }
            mAppDatabase!!.getChatDao().update(chatModel)
        } else {
            val mReceiverId = if (chatModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
                chatModel.receiver
            } else {
                chatModel.sender
            }

            chatModel.userId = SharedPreferenceEditor.getData(USER_ID)
            val listFriends = mAppDatabase!!.getUserDetailsDao().getUserDetails(mReceiverId)
            if (listFriends.isEmpty()) {
                val mAddFriendsList = ArrayList<AddFriendUserModel.DataModel>()
                val mDataModel = AddFriendUserModel().DataModel()
                mDataModel.id = mReceiverId
                mAddFriendsList.add(mDataModel)
                val mAddFriendUserModel = AddFriendUserModel()
                mAddFriendUserModel.data = mAddFriendsList
                EventBus.getDefault().post(mAddFriendUserModel)
            }
            val listMessageType = mAppDatabase!!.getChatDao()
                .getChatType(
                    mReceiverId,
                    SharedPreferenceEditor.getData(USER_ID),
                    ChatMessageTypes.UNREAD.type
                )
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val taskInfo = am.getRunningTasks(1)
            if (taskInfo[0].topActivity!!.className == "com.sparkout.chat.ui.chat.view.ChatActivity") {
                if (mReceiverId != ChatActivity.mToId) {
                    if (listMessageType.isEmpty()) {
                        val mMessageId = getUTCTime()
                            .plus("-").plus(SharedPreferenceEditor.getData(USER_ID)).plus("-")
                            .plus(mReceiverId)
                        val chatModelUnread = ChatModel()
                        chatModelUnread.receiver = mReceiverId
                        chatModelUnread.sender = SharedPreferenceEditor.getData(USER_ID)
                        chatModelUnread.messageId = getMessageId(mMessageId)
                        chatModelUnread.messageStatus = ChatMessageStatus.READ.ordinal
                        chatModelUnread.messageType = ChatMessageTypes.UNREAD.type
                        chatModelUnread.chatTime = chatModel.chatTime
                        chatModelUnread.chatType = ChatTypes.SINGLE.type

                        mAppDatabase!!.getChatDao().insert(chatModelUnread)
                    }
                }
            } else {
                if (listMessageType.isEmpty()) {
                    val mMessageId = getUTCTime()
                        .plus("-").plus(SharedPreferenceEditor.getData(USER_ID)).plus("-")
                        .plus(mReceiverId)
                    val chatModelUnread = ChatModel()
                    chatModelUnread.receiver = mReceiverId
                    chatModelUnread.sender = SharedPreferenceEditor.getData(USER_ID)
                    chatModelUnread.messageId = getMessageId(mMessageId)
                    chatModelUnread.messageStatus = ChatMessageStatus.READ.ordinal
                    chatModelUnread.messageType = ChatMessageTypes.UNREAD.type
                    chatModelUnread.chatTime = chatModel.chatTime
                    chatModelUnread.chatType = ChatTypes.SINGLE.type

                    mAppDatabase!!.getChatDao().insert(chatModelUnread)
                }
            }

            if (chatModel.messageType == ChatMessageTypes.AUDIO.type) {
                val listAudioMessage =
                    mAppDatabase!!.getAudioStatusDao().getAudioMessage(
                        chatModel.messageId,
                        SharedPreferenceEditor.getData(USER_ID)
                    )
                if (listAudioMessage.isNotEmpty()) {
                    val audioStatus = AudioStatus()
                    audioStatus.userId = SharedPreferenceEditor.getData(USER_ID)
                    audioStatus.messageId = chatModel.messageId
                    audioStatus.checkPlayed = false
                    mAppDatabase!!.getAudioStatusDao().update(audioStatus)
                } else {
                    val audioStatus = AudioStatus()
                    audioStatus.userId = SharedPreferenceEditor.getData(USER_ID)
                    audioStatus.messageId = chatModel.messageId
                    audioStatus.checkPlayed = false
                    mAppDatabase!!.getAudioStatusDao().insert(audioStatus)
                }
            }

            if (chatModel.messageType == ChatMessageTypes.LOCATION.type) {
                val mLocationModel = LocationModel()
                mLocationModel.messageId = chatModel.messageId
                mLocationModel.latitude = chatModel.latitude!!
                mLocationModel.longitude = chatModel.longitude!!
                mAppDatabase!!.getLocationDao().insert(mLocationModel)
            }
            mAppDatabase!!.getChatDao().insert(chatModel)
        }
        lastMessageUpdate(context, chatModel)
    }
    private val mSingleChatTyping = Emitter.Listener {
        val chatTypingModel =
            GsonBuilder().create().fromJson(it[0].toString(), ChatTypingModel::class.java)
        Timber.tag("Krish").e("mSingleChatTyping: ${Gson().toJson(chatTypingModel)}")
        //        ackEvent(context, SINGLE_CHAT_TYPING)
        EventBus.getDefault().post(chatTypingModel)
    }
    private val mSingleChatDelivered = Emitter.Listener {
        val chatDelieverModel =
            GsonBuilder().create().fromJson(it[0].toString(), ChatDelieverModel::class.java)
        Timber.tag("Krish").e("mSingleChatDelivered: ${Gson().toJson(chatDelieverModel)}")
        //        ackEvent(context, SINGLE_CHAT_DELIVERED)
        val chatModel = mAppDatabase!!.getChatDao()
            .getChatData(chatDelieverModel.messageId, SharedPreferenceEditor.getData(USER_ID))
        if (chatModel.isNotEmpty()) {
            if (chatModel[0].messageStatus < ChatMessageStatus.DELIVERED.ordinal) {
                chatModel[0].messageStatus = ChatMessageStatus.DELIVERED.ordinal
            }
            mAppDatabase!!.getChatDao().update(chatModel[0])
            EventBus.getDefault().post(chatModel[0])
            EventBus.getDefault().post("message_update")
        }
    }
    private val mSingleChatRead = Emitter.Listener {
        val chatReadModel =
            GsonBuilder().create().fromJson(it[0].toString(), ChatReadModel::class.java)
        Timber.tag("Krish").e("mSingleChatRead: ${Gson().toJson(chatReadModel)}")
        //        ackEvent(context, SINGLE_CHAT_READ)
        if (chatReadModel.receiver == SharedPreferenceEditor.getData(USER_ID)) {
            val listMessageId = chatReadModel.messageId
            for (i in listMessageId.indices) {
                val mMessageId = listMessageId[i]
                val listChatRead =
                    mAppDatabase!!.getChatDao()
                        .getChatData(mMessageId, SharedPreferenceEditor.getData(USER_ID))
                if (listChatRead.isNotEmpty()) {
                    for (j in listChatRead.indices) {
                        listChatRead[j].messageStatus = ChatMessageStatus.READ.ordinal
                        mAppDatabase!!.getChatDao().update(listChatRead[j])
                    }
                }
            }
            EventBus.getDefault().post("messenger")
            EventBus.getDefault().post("message_update")
        }
    }
    private val mProfileUpdate = Emitter.Listener {
        val userDetailsModel =
            GsonBuilder().create().fromJson(it[0].toString(), UserDetailsModel::class.java)
        Timber.tag("Krish").e("mProfileUpdate: ${Gson().toJson(userDetailsModel)}")
        //        ackEvent(context, PROFILE_UPDATED)
        val listUserDetails = mAppDatabase!!.getUserDetailsDao().getUserDetails(userDetailsModel.id)
        if (listUserDetails.isNotEmpty()) {
            mAppDatabase!!.getUserDetailsDao().update(userDetailsModel)
        } else {
            mAppDatabase!!.getUserDetailsDao().insert(userDetailsModel)
        }
        val listChat =
            mAppDatabase!!.getChatListDao()
                .getChatList(userDetailsModel.id, SharedPreferenceEditor.getData(USER_ID))
        if (listChat.isNotEmpty()) {
            userDetailsModel.username?.let {
                listChat[0].name = it
            }

            userDetailsModel.profilePicture?.let {
                listChat[0].profilePicture = it
            }

            mAppDatabase!!.getChatListDao().update(listChat[0])
        }
        EventBus.getDefault().post("messenger")
        EventBus.getDefault().post("message_update")
    }
    private val mContactLastSeen = Emitter.Listener {
        val userStatusModel =
            GsonBuilder().create().fromJson(it[0].toString(), UserStatusModel::class.java)
        Timber.tag("Krish").e("mContactLastSeen: ${Gson().toJson(userStatusModel)}")
        if (userStatusModel.id != SharedPreferenceEditor.getData(USER_ID)) {
            val listUserDetails =
                mAppDatabase!!.getUserDetailsDao().getUserDetails(userStatusModel.id)
            if (listUserDetails.isNotEmpty()) {
                listUserDetails[0].lastSeen = userStatusModel.lastSeen
                mAppDatabase!!.getUserDetailsDao().update(listUserDetails[0])
                EventBus.getDefault().post("user_status")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val mGetMyContacts = Emitter.Listener {
        //        ackEvent(context, GET_MY_CONTACTS)
        /*val getMyContactsModel =
            GsonBuilder().create().fromJson(it[0].toString(), GetMyContactsModel::class.java)
        Timber.tag("Krish").e("mGetMyContacts: ${Gson().toJson(getMyContactsModel)}")
        val listMyContacts = getMyContactsModel.contacts
        if (listMyContacts.isNotEmpty()) {
            for (i in 0 until listMyContacts.size) {
                val checkFriendExists =
                    mAppDatabase!!.getFriendDetailsDao()
                        .checkFriendAlreadyExists(listMyContacts[i].id, SharedPreferenceEditor.getData(USER_ID))
                if (checkFriendExists.isEmpty()) {
                    val friendDetailModel = FriendDetailsModel()
                    val mObjectId = SharedPreferenceEditor.getData(USER_ID).plus("-").plus(listMyContacts[i].id)
                    friendDetailModel.objectId = getObjectId(mObjectId)
                    friendDetailModel.sender = SharedPreferenceEditor.getData(USER_ID)
                    friendDetailModel.receiver = listMyContacts[i].id
                    mAppDatabase!!.getFriendDetailsDao().insert(friendDetailModel)
                }
                val listUserDetails =
                    mAppDatabase!!.getUserDetailsDao().getUserDetails(listMyContacts[i].id)
                if (listUserDetails.isNotEmpty()) {
                    mAppDatabase!!.getUserDetailsDao().update(listMyContacts[i])
                } else {
                    mAppDatabase!!.getUserDetailsDao().insert(listMyContacts[i])
                }
            }
        }*/
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val mAddContact = Emitter.Listener {
        //        ackEvent(context, ADD_CONTACT)
        /*var mReceiverId: String = ""
        mReceiverId = if (friendDetailsModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
            friendDetailsModel.receiver
        } else {
            friendDetailsModel.sender
        }
        val checkFriendExists =
            mAppDatabase!!.getFriendDetailsDao()
                .checkFriendAlreadyExists(mReceiverId, SharedPreferenceEditor.getData(USER_ID))
        if (checkFriendExists.isEmpty()) {
            val friendDetailModel = FriendDetailsModel()
            val mObjectId = SharedPreferenceEditor.getData(USER_ID).plus("-").plus(mReceiverId)
            friendDetailModel.objectId = getObjectId(mObjectId)
            friendDetailModel.sender = SharedPreferenceEditor.getData(USER_ID)
            friendDetailModel.receiver = mReceiverId
            mAppDatabase!!.getFriendDetailsDao().insert(friendDetailModel)
        }
        val userDetailsModel =
            mAppDatabase!!.getUserDetailsDao().getUserDetails(mReceiverId)
        if (userDetailsModel.isEmpty()) {
            mAppDatabase!!.getUserDetailsDao().insert(friendDetailsModel.user!!)
        } else {
            mAppDatabase!!.getUserDetailsDao().update(friendDetailsModel.user!!)
        }*/
    }
    private val mAddedInNewGroup = Emitter.Listener {
        Timber.tag("Krish").e("mAddedInNewGroup_before: ${Gson().toJson(it[0])}")
        val mGroupModel =
            GsonBuilder().create().fromJson(it[0].toString(), GroupModel::class.java)
        Timber.tag("Krish").e("mAddedInNewGroup: ${Gson().toJson(mGroupModel)}")
        //ackEvent(context, ADDED_IN_NEW_GROUP_EVENT)
        val mGroupDetailsModel = mGroupModel.group
        mGroupDetailsModel.userId = SharedPreferenceEditor.getData(USER_ID)
        val mGroupId = mGroupDetailsModel.groupId
        val mGroupMemberList = mGroupModel.members
        val mNewFriendsList = ArrayList<String>()
        for (i in 0 until mGroupMemberList.size) {
            if (mAppDatabase!!.getUserDetailsDao().getUserDetails(mGroupMemberList[i].id)
                    .isEmpty()
            ) {
                mNewFriendsList.add(mGroupMemberList[i].id)
            }
            val mGroupMember = mAppDatabase!!.getGroupMemberDao()
                .getGroupMemberDetails(
                    mGroupId,
                    mGroupMemberList[i].id,
                    SharedPreferenceEditor.getData(USER_ID)
                )
            if (mGroupMember.isNotEmpty()) {
                mGroupMember[0].checkRemoved = false
                mGroupMember[0].userId = SharedPreferenceEditor.getData(USER_ID)
                mAppDatabase!!.getGroupMemberDao().update(mGroupMember[0])
            } else {
                val mGroupMemberModel = GroupMemberModel()
                mGroupMemberModel.checkAdmin = mGroupMemberList[i].checkAdmin!!
                mGroupMemberModel.checkRemoved = false
                mGroupMemberModel.groupId = mGroupId
                mGroupMemberModel.userId = SharedPreferenceEditor.getData(USER_ID)
                mGroupMemberModel.memberId = mGroupMemberList[i].id
                mGroupMemberModel.objectId =
                    getObjectId(mGroupId.plus("-").plus(mGroupMemberList[i].id))
                mAppDatabase!!.getGroupMemberDao().insert(mGroupMemberModel)
            }
        }
        if (mNewFriendsList.isNotEmpty()) {
            val mAddFriendsList = ArrayList<AddFriendUserModel.DataModel>()
            for (i in 0 until mNewFriendsList.size) {
                val mDataModel = AddFriendUserModel()
                    .DataModel()
                mDataModel.id = mNewFriendsList[i]
                mAddFriendsList.add(mDataModel)
            }
            val mAddFriendUserModel =
                AddFriendUserModel()
            mAddFriendUserModel.data = mAddFriendsList
            EventBus.getDefault().post(mAddFriendUserModel)
            /*val mGetUsersInfoModel = GetGroupMembersModel()
            mGetUsersInfoModel.id = mNewFriendsList
            mGetUsersInfoModel.sender = SharedPreferenceEditor.getData(USER_ID)
            val mGetUserInfo = Gson().toJson(mGetUsersInfoModel)
            val mUserInfoObject = JSONObject(mGetUserInfo)
            getUserInfo(mUserInfoObject)*/
        }
        val mGroupList =
            mAppDatabase!!.getGroupDetailsDao()
                .getGroupDetails(mGroupId, SharedPreferenceEditor.getData(USER_ID))
        if (mGroupList.isNotEmpty()) {
            mAppDatabase!!.getGroupDetailsDao().update(mGroupDetailsModel)
        } else {
            mAppDatabase!!.getGroupDetailsDao().insert(mGroupDetailsModel)
        }
        EventBus.getDefault().post("message_update")
        EventBus.getDefault().post(mGroupId)
    }
    private val mGroupChatMessage = Emitter.Listener {
        val chatModel = GsonBuilder().create().fromJson(it[0].toString(), ChatModel::class.java)
        Timber.tag("Krish").e("mGroupChatMessage: ${Gson().toJson(chatModel)}")
        //        ackEvent(context, GROUP_CHAT_MESSAGE_EVENT)
        val listGroupDetails =
            mAppDatabase!!.getGroupDetailsDao()
                .getGroupDetails(chatModel.receiver, SharedPreferenceEditor.getData(USER_ID))
        if (listGroupDetails.isEmpty()) {
            val mGroupInfoSocketModel = GetGroupInfoSocketModel()
            mGroupInfoSocketModel.groupId = chatModel.receiver
            mGroupInfoSocketModel.sender = SharedPreferenceEditor.getData(USER_ID)
            val mGroupInfoJson = Gson().toJson(mGroupInfoSocketModel)
            val jsonObject = JSONObject(mGroupInfoJson)
            getGroupInfoEvent(jsonObject)
        }
        val listReceivedChat =
            mAppDatabase!!.getChatDao()
                .getChatData(chatModel.messageId, SharedPreferenceEditor.getData(USER_ID))
        val mMessageStatusList =
            mAppDatabase!!.getMessageStatusDao()
                .messageStatusList(
                    chatModel.receiver,
                    chatModel.messageId,
                    SharedPreferenceEditor.getData(USER_ID)
                )
        if (mMessageStatusList.isNotEmpty()) {
            val mMessageStatusModel = MessageStatusModel()
            mMessageStatusModel.groupId = chatModel.receiver
            mMessageStatusModel.messageId = chatModel.messageId
            mMessageStatusModel.deliveredId = ArrayList<String>()
            mMessageStatusModel.seenId = ArrayList<String>()
            mAppDatabase!!.getMessageStatusDao().update(mMessageStatusModel)
        } else {
            val mMessageStatusModel = MessageStatusModel()
            mMessageStatusModel.groupId = chatModel.receiver
            mMessageStatusModel.messageId = chatModel.messageId
            mMessageStatusModel.deliveredId = ArrayList<String>()
            mMessageStatusModel.seenId = ArrayList<String>()
            mAppDatabase!!.getMessageStatusDao().insert(mMessageStatusModel)
        }

        if (listReceivedChat.isNotEmpty()) {
            if (chatModel.messageStatus == ChatMessageStatus.NOT_SENT.ordinal) {
                chatModel.messageStatus = ChatMessageStatus.SENT.ordinal
            } else {
                chatModel.messageStatus = chatModel.messageStatus
            }
            mAppDatabase!!.getChatDao().update(chatModel)
        } else {
            val mReceiverId = chatModel.receiver
            chatModel.userId = SharedPreferenceEditor.getData(USER_ID)
            val listMessageType = mAppDatabase!!.getChatDao()
                .getChatType(
                    mReceiverId,
                    SharedPreferenceEditor.getData(USER_ID),
                    ChatMessageTypes.UNREAD.type
                )
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val taskInfo = am.getRunningTasks(1)
            if (taskInfo[0].topActivity!!.className == "com.sparkout.chat.ui.groupchat.view.GroupChatActivity") {
                if (mReceiverId != GroupChatActivity.mToId) {
                    if (listMessageType.isEmpty() && !getInfoMessageTypes(chatModel.messageType)) {
                        val mMessageId = getUTCTime()
                            .plus("-").plus(SharedPreferenceEditor.getData(USER_ID)).plus("-")
                            .plus(mReceiverId)
                        val chatModelUnread = ChatModel()
                        chatModelUnread.receiver = mReceiverId
                        chatModelUnread.sender = SharedPreferenceEditor.getData(USER_ID)
                        chatModelUnread.messageId = getMessageId(mMessageId)
                        chatModelUnread.messageStatus = ChatMessageStatus.READ.ordinal
                        chatModelUnread.messageType = ChatMessageTypes.UNREAD.type
                        chatModelUnread.chatTime = chatModel.chatTime
                        chatModelUnread.chatType = ChatTypes.GROUP.type

                        mAppDatabase!!.getChatDao().insert(chatModelUnread)
                    }
                }
            } else {
                if (listMessageType.isEmpty() && !getInfoMessageTypes(chatModel.messageType)) {
                    val mMessageId = getUTCTime()
                        .plus("-").plus(SharedPreferenceEditor.getData(USER_ID)).plus("-")
                        .plus(mReceiverId)
                    val chatModelUnread = ChatModel()
                    chatModelUnread.receiver = mReceiverId
                    chatModelUnread.sender = SharedPreferenceEditor.getData(USER_ID)
                    chatModelUnread.messageId = getMessageId(mMessageId)
                    chatModelUnread.messageStatus = ChatMessageStatus.READ.ordinal
                    chatModelUnread.messageType = ChatMessageTypes.UNREAD.type
                    chatModelUnread.chatTime = chatModel.chatTime
                    chatModelUnread.chatType = ChatTypes.GROUP.type

                    mAppDatabase!!.getChatDao().insert(chatModelUnread)
                }
            }

            if (chatModel.messageStatus == ChatMessageStatus.NOT_SENT.ordinal) {
                chatModel.messageStatus = ChatMessageStatus.SENT.ordinal
            } else {
                chatModel.messageStatus = chatModel.messageStatus
            }
            if (chatModel.messageType != ChatMessageTypes.TEXT.type) {
                if (chatModel.messageType ==
                    ChatMessageTypes.IMAGE.type
                ) {
                    if (chatModel.sender != SharedPreferenceEditor.getData(USER_ID)) {
                        if (null == BaseUtils.loadImagePath(chatModel.messageId)) {
                            startWorker(
                                context, chatModel.uri!!,
                                chatModel.messageType,
                                chatModel.messageId,
                                mReceiverId
                            )
                        }
                    }
                } else if (chatModel.messageType == ChatMessageTypes.AUDIO.type) {
                    if (null == BaseUtils.loadAudioPath(
                            context, chatModel.messageId, 2
                        )
                    ) {
                        startWorker(
                            context, chatModel.uri!!,
                            chatModel.messageType,
                            chatModel.messageId,
                            mReceiverId
                        )
                    }
                } else if (chatModel.messageType == ChatMessageTypes.VIDEO.type) {
                    if (chatModel.sender != SharedPreferenceEditor.getData(USER_ID)) {
                        val videoDirectoryPath =
                            Environment.getExternalStorageDirectory().toString().plus("/")
                                .plus(Environment.DIRECTORY_PICTURES).plus("/")
                                .plus("Bee Bush Messenger Videos").plus("/")
                                .plus(chatModel.messageId)
                                .plus(".mp4")
                        if (!fileExist(videoDirectoryPath)) {
                            startWorker(
                                context, chatModel.uri!!,
                                chatModel.messageType,
                                chatModel.messageId,
                                mReceiverId
                            )
                        }
                    }
                } else if (chatModel.messageType == ChatMessageTypes.DOCUMENT.type) {
                    if (chatModel.sender != SharedPreferenceEditor.getData(USER_ID)) {
                        if (!fileExist(sDocumentDirectoryPath + "/" + chatModel.messageId + ".pdf")) {
                            /*val listLocalData =
                                mAppDatabase!!.getLocalDataDao().getLocalData(chatModel.messageId)
                            if (listLocalData.isEmpty()) {
                                download(context,
                                         chatModel.uri!!,
                                         chatModel.messageType,
                                         chatModel.messageId,
                                         mReceiverId)
                            }*/
                        }
                    }
                }
            }
            if (chatModel.messageType == ChatMessageTypes.CREATEGROUP.type) {
                val listCheckMessageType = mAppDatabase!!.getChatDao()
                    .checkMessageType(
                        ChatMessageTypes.CREATEGROUP.type,
                        mReceiverId,
                        SharedPreferenceEditor.getData(USER_ID)
                    )
                if (listCheckMessageType.isEmpty()) {
                    mAppDatabase!!.getChatDao().insert(chatModel)
                }
            } else {
                mAppDatabase!!.getChatDao().insert(chatModel)
            }
        }
        lastGroupMessageUpdate(context, chatModel)
    }
    private val mGroupInfoListener = Emitter.Listener {
        val mGroupInfoModel =
            GsonBuilder().create().fromJson(it[0].toString(), GroupInfoModel::class.java)
        Timber.tag("Krish").e("mGroupInfoListener: ${Gson().toJson(mGroupInfoModel)}")
        //        ackEvent(context, GROUP_INFO_UPDATE_EVENT)
        val mGroupModel =
            mAppDatabase!!.getGroupDetailsDao()
                .getGroupDetails(mGroupInfoModel.groupId!!, SharedPreferenceEditor.getData(USER_ID))
        if (mGroupModel.isNotEmpty()) {
            if (null != mGroupInfoModel.groupTitle) {
                if (mGroupInfoModel.groupTitle!!.isNotEmpty()) {
                    mGroupModel[0].groupTitle = mGroupInfoModel.groupTitle!!
                }
            }
            if (null != mGroupInfoModel.groupDescription) {
                if (mGroupInfoModel.groupDescription!!.isNotEmpty()) {
                    mGroupModel[0].groupDescription = mGroupInfoModel.groupDescription!!
                }
            }
            if (null != mGroupInfoModel.groupImage) {
                if (mGroupInfoModel.groupImage!!.isNotEmpty()) {
                    mGroupModel[0].groupImage = mGroupInfoModel.groupImage!!
                }
            }
            mAppDatabase!!.getGroupDetailsDao().update(mGroupModel[0])
            EventBus.getDefault().post(mGroupModel[0].groupId)
        }
    }
    private val mGroupAddMemberListener = Emitter.Listener {
        val mGroupAddMemberModel =
            GsonBuilder().create().fromJson(it[0].toString(), GroupAddMember::class.java)
        Timber.tag("Krish").e("mGroupAddMemberListener: ${Gson().toJson(mGroupAddMemberModel)}")
        //        ackEvent(context, GROUP_ADD_MEMBER_EVENT)
        val mGroupId = mGroupAddMemberModel.groupId
        val mNewMembersList = mGroupAddMemberModel.newMembers
        val mNewFriendsList = ArrayList<String>()
        for (i in 0 until mNewMembersList.size) {
            val mGroupMember = mAppDatabase!!.getGroupMemberDao()
                .getGroupMemberDetails(
                    mGroupId,
                    mNewMembersList[i].id,
                    SharedPreferenceEditor.getData(USER_ID)
                )
            if (mGroupMember.isNotEmpty()) {
                mGroupMember[0].checkRemoved = false
                mAppDatabase!!.getGroupMemberDao().update(mGroupMember[0])
            } else {
                val mGroupMemberModel = GroupMemberModel()
                mGroupMemberModel.checkAdmin = mNewMembersList[i].checkAdmin!!
                mGroupMemberModel.checkRemoved = false
                mGroupMemberModel.groupId = mGroupId
                mGroupMemberModel.memberId = mNewMembersList[i].id
                mGroupMemberModel.objectId =
                    getObjectId(mGroupId.plus("-").plus(mNewMembersList[i].id))
                mAppDatabase!!.getGroupMemberDao().insert(mGroupMemberModel)
            }

            if (mAppDatabase!!.getUserDetailsDao().getUserDetails(mNewMembersList[i].id)
                    .isEmpty()
            ) {
                mNewFriendsList.add(mNewMembersList[i].id)
            }
        }
        if (mNewFriendsList.isNotEmpty()) {
            val mAddFriendsList = ArrayList<AddFriendUserModel.DataModel>()
            for (i in 0 until mNewFriendsList.size) {
                val mDataModel = AddFriendUserModel()
                    .DataModel()
                mDataModel.id = mNewFriendsList[i]
                mAddFriendsList.add(mDataModel)
            }
            val mAddFriendUserModel =
                AddFriendUserModel()
            mAddFriendUserModel.data = mAddFriendsList
            EventBus.getDefault().post(mAddFriendUserModel)
            /*val mGetUsersInfoModel = GetGroupMembersModel()
            mGetUsersInfoModel.id = mNewFriendsList
            mGetUsersInfoModel.sender = SharedPreferenceEditor.getData(USER_ID)
            val mGetUserInfo = Gson().toJson(mGetUsersInfoModel)
            val mUserInfoObject = JSONObject(mGetUserInfo)
            getUserInfo(mUserInfoObject)*/
        }
        EventBus.getDefault().post(mGroupId)
    }
    private val mGroupRemoveMemberListener = Emitter.Listener {
        val mGroupRemoveMemberModel =
            GsonBuilder().create().fromJson(it[0].toString(), GroupRemoveMember::class.java)
        Timber.tag("Krish")
            .e("mGroupRemoveMemberListener: ${Gson().toJson(mGroupRemoveMemberModel)}")
        //        ackEvent(context, GROUP_REMOVE_MEMBER_EVENT)
        val mGroupId = mGroupRemoveMemberModel.groupId
        val mRemovedMembersList = mGroupRemoveMemberModel.removedMembers
        for (i in mRemovedMembersList.indices) {
            if (mRemovedMembersList[i] == SharedPreferenceEditor.getData(USER_ID)) {
                mAppDatabase!!.getGroupDetailsDao()
                    .exitFromGroup(mGroupId, SharedPreferenceEditor.getData(USER_ID))
            }
            mAppDatabase!!.getGroupMemberDao().removeMember(mGroupId, mRemovedMembersList[i])
        }
        EventBus.getDefault().post(mGroupId)
    }
    private val mExitGroupListener = Emitter.Listener {
        val mGroupChatExitModel =
            GsonBuilder().create().fromJson(it[0].toString(), GroupChatExitModel::class.java)
        Timber.tag("Krish")
            .e("mExitGroupListener: ${Gson().toJson(mGroupChatExitModel)}")
        //        ackEvent(context, GROUP_EXIT_MEMBER_EVENT)
        val mGroupId = mGroupChatExitModel.groupId
        val sender = mGroupChatExitModel.sender
        if (sender == SharedPreferenceEditor.getData(USER_ID)) {
            mAppDatabase!!.getGroupDetailsDao()
                .exitFromGroup(mGroupId, SharedPreferenceEditor.getData(USER_ID))
        }
        mAppDatabase!!.getGroupMemberDao().removeMember(mGroupId, sender)
        EventBus.getDefault().post(mGroupId)
    }
    private val mAddAdminListener = Emitter.Listener {
        val mAddOrRemoveAdminModel =
            GsonBuilder().create().fromJson(it[0].toString(), AddOrRemoveAdminModel::class.java)
        Timber.tag("Krish")
            .e("mAddAdminListener: ${Gson().toJson(mAddOrRemoveAdminModel)}")
        //        ackEvent(context, GROUP_ADD_ADMIN_EVENT)
        val mGroupId = mAddOrRemoveAdminModel.groupId
        val mMemberId = mAddOrRemoveAdminModel.admin
        mAppDatabase!!.getGroupMemberDao().addOrRemoveAdmin(mGroupId!!, mMemberId!!, 1)
        EventBus.getDefault().post(mGroupId)
    }
    private val mRemoveAdminListener = Emitter.Listener {
        val mAddOrRemoveAdminModel =
            GsonBuilder().create().fromJson(it[0].toString(), AddOrRemoveAdminModel::class.java)
        Timber.tag("Krish")
            .e("mRemoveAdminListener: ${Gson().toJson(mAddOrRemoveAdminModel)}")
        //        ackEvent(context, GROUP_REMOVE_ADMIN_EVENT)
        val mGroupId = mAddOrRemoveAdminModel.groupId
        val mMemberId = mAddOrRemoveAdminModel.admin
        mAppDatabase!!.getGroupMemberDao().addOrRemoveAdmin(mGroupId!!, mMemberId!!, 0)
        EventBus.getDefault().post(mGroupId)
    }
    private val mGroupChatDeliveredListener = Emitter.Listener {
        val mGroupChatDeliveredModel =
            GsonBuilder().create().fromJson(it[0].toString(), GroupChatDeliveredModel::class.java)
        Timber.tag("Krish")
            .e("mGroupChatDeliveredListener: ${Gson().toJson(mGroupChatDeliveredModel)}")
        //        ackEvent(context, GROUP_CHAT_DELIVERED_EVENT)
        val mGroupId = mGroupChatDeliveredModel.groupId
        val mMessageId = mGroupChatDeliveredModel.messageId
        val mSenderId = mGroupChatDeliveredModel.sender
        val mMessageStatusList =
            mAppDatabase!!.getMessageStatusDao()
                .messageStatusList(mGroupId, mMessageId, SharedPreferenceEditor.getData(USER_ID))
        if (mMessageStatusList.isNotEmpty()) {
            for (i in mMessageStatusList.indices) {
                if (!mMessageStatusList[i].deliveredId.contains(mSenderId)) {
                    mMessageStatusList[i].deliveredId.add(mSenderId)
                    mAppDatabase!!.getMessageStatusDao().update(mMessageStatusList[i])
                }
            }
        }
        val mUpdatedMessageStatusList =
            mAppDatabase!!.getMessageStatusDao()
                .messageStatusList(mGroupId, mMessageId, SharedPreferenceEditor.getData(USER_ID))
        if (mUpdatedMessageStatusList.isNotEmpty()) {
            val mGroupMembersId = ArrayList<String>()
            val mListGroupMembers =
                mAppDatabase!!.getGroupMemberDao()
                    .getGroupMembers(mGroupId, SharedPreferenceEditor.getData(USER_ID))
            if (mListGroupMembers.isNotEmpty()) {
                for (i in 0 until mListGroupMembers.size) {
                    mGroupMembersId.add(mListGroupMembers[i].memberId)
                }
            }
            var mDeliveredToEveryone = true
            if (mGroupMembersId.isNotEmpty()) {
                for (i in mGroupMembersId.indices) {
                    if (!mUpdatedMessageStatusList[0].deliveredId.contains(mGroupMembersId[i]) &&
                        mGroupMembersId[i] != SharedPreferenceEditor.getData(USER_ID)
                    ) {
                        mDeliveredToEveryone = false
                    }
                }
            }
            if (mDeliveredToEveryone) {
                val chatModel =
                    mAppDatabase!!.getChatDao()
                        .getGroupChatData(mMessageId, SharedPreferenceEditor.getData(USER_ID))
                if (chatModel.isNotEmpty()) {
                    if (chatModel[0].messageStatus < ChatMessageStatus.DELIVERED.ordinal) {
                        chatModel[0].messageStatus = ChatMessageStatus.DELIVERED.ordinal
                    }
                    mAppDatabase!!.getChatDao().update(chatModel[0])
                    EventBus.getDefault().post("messenger")
                }
            }
        }
    }
    private val mGroupChatReadListener = Emitter.Listener {
        val mGroupChatReadModel =
            GsonBuilder().create().fromJson(it[0].toString(), GroupChatReadModel::class.java)
        Timber.tag("Krish")
            .e("mGroupChatReadListener: ${Gson().toJson(mGroupChatReadModel)}")
        //        ackEvent(context, GROUP_CHAT_READ_EVENT)
        val mMessageId = mGroupChatReadModel.messageId
        val mGroupId = mGroupChatReadModel.groupId
        val mSenderId = mGroupChatReadModel.sender
        if (mMessageId.isNotEmpty()) {
            for (i in mMessageId.indices) {
                val mMessageStatusList =
                    mAppDatabase!!.getMessageStatusDao()
                        .messageStatusList(
                            mGroupId,
                            mMessageId[i],
                            SharedPreferenceEditor.getData(USER_ID)
                        )
                if (mMessageStatusList.isNotEmpty()) {
                    for (j in mMessageStatusList.indices) {
                        if (!mMessageStatusList[j].seenId.contains(mSenderId)) {
                            mMessageStatusList[j].seenId.add(mSenderId)
                            mAppDatabase!!.getMessageStatusDao().update(mMessageStatusList[j])
                        }
                    }
                }
                val mUpdatedMessageStatusList =
                    mAppDatabase!!.getMessageStatusDao()
                        .messageStatusList(
                            mGroupId,
                            mMessageId[i],
                            SharedPreferenceEditor.getData(USER_ID)
                        )
                if (mUpdatedMessageStatusList.isNotEmpty()) {
                    val mGroupMembersId = ArrayList<String>()
                    val mListGroupMembers =
                        mAppDatabase!!.getGroupMemberDao()
                            .getGroupMembers(mGroupId, SharedPreferenceEditor.getData(USER_ID))
                    if (mListGroupMembers.isNotEmpty()) {
                        for (j in mListGroupMembers.indices) {
                            mGroupMembersId.add(mListGroupMembers[j].memberId)
                        }
                    }
                    var mSeenByEveryone = true
                    if (mGroupMembersId.isNotEmpty()) {
                        for (j in mGroupMembersId.indices) {
                            if (!mUpdatedMessageStatusList[0].seenId.contains(mGroupMembersId[j]) &&
                                mGroupMembersId[j] != SharedPreferenceEditor.getData(USER_ID)
                            ) {
                                mSeenByEveryone = false
                            }
                        }
                    }
                    if (mSeenByEveryone) {
                        val chatModel = mAppDatabase!!.getChatDao()
                            .getGroupChatData(
                                mMessageId[i],
                                SharedPreferenceEditor.getData(USER_ID)
                            )
                        if (chatModel.isNotEmpty()) {
                            if (chatModel[0].messageStatus < ChatMessageStatus.READ.ordinal) {
                                chatModel[0].messageStatus = ChatMessageStatus.READ.ordinal
                            }
                            mAppDatabase!!.getChatDao().update(chatModel[0])
                        }
                    }
                }
            }
            EventBus.getDefault().post("messenger")
            EventBus.getDefault().post("update_deliver_read")
        }
    }
    private val mReportMessageListener = Emitter.Listener {
        val mReportMessageModel =
            GsonBuilder().create().fromJson(it[0].toString(), ReportMessageModel::class.java)
        Timber.tag("Krish")
            .e("mReportMessageListener: ${Gson().toJson(mReportMessageModel)}")
        //        ackEvent(context, REPORT_MESSAGE_EVENT)
        Handler(Looper.getMainLooper()).post(object : Runnable {
            override fun run() {
                showToast(context, "Report sent")
            }
        })
    }
    private val mGetUserInfoListener = Emitter.Listener {
        Timber.tag("Krish").e("mGetUserInfoListener: ${Gson().toJson(it[0])}")
        //        ackEvent(context, GET_USERS_INFO_EVENT)
        val mJsonArray = it[0] as JSONArray
        if (mJsonArray.length() != 0) {
            for (i in 0 until mJsonArray.length()) {
                val mJsonObject = mJsonArray.optJSONObject(i)
                val mUserInfoModel = GsonBuilder().create()
                    .fromJson(mJsonObject.toString(), UserInfoModel::class.java)
                val listUserDetails =
                    mAppDatabase!!.getUserDetailsDao().getUserDetails(mUserInfoModel.id)
                if (listUserDetails.isNotEmpty()) {
                    val mUserDetailsModel = UserDetailsModel()
                    mUserDetailsModel.username = mUserInfoModel.username
                    mUserDetailsModel.profilePicture = mUserInfoModel.profilePicture
                    mUserDetailsModel.id = mUserInfoModel.id
                    mUserDetailsModel.lastSeen = mUserInfoModel.lastSeen
                    mAppDatabase!!.getUserDetailsDao().update(mUserDetailsModel)
                } else {
                    val mUserDetailsModel = UserDetailsModel()
                    mUserDetailsModel.username = mUserInfoModel.username
                    mUserDetailsModel.profilePicture = mUserInfoModel.profilePicture
                    mUserDetailsModel.id = mUserInfoModel.id
                    mUserDetailsModel.lastSeen = mUserInfoModel.lastSeen
                    mAppDatabase!!.getUserDetailsDao().insert(mUserDetailsModel)
                }
            }
            EventBus.getDefault().post("message_update")
        }
    }
    private val mMessageDeletedOnGroupListener = Emitter.Listener {
        val mDeletedOnGroupModel =
            GsonBuilder().create()
                .fromJson(it[0].toString(), DeletedOnGroupSocketModel::class.java)
        Timber.tag("Krish")
            .e("mMessageDeletedOnGroupListener: ${Gson().toJson(mDeletedOnGroupModel)}")
        //        ackEvent(context, MESSAGE_DELETED_ON_GROUP_EVENT)
        val mMessageId = mDeletedOnGroupModel.messageId
        val mGroupId = mDeletedOnGroupModel.groupId
        mAppDatabase!!.getChatDao()
            //            .updateMessageForDeleteForEveryone(mMessageId, SharedPreferenceEditor.getData(USER_ID))
            .updateMessageForDeleteForEveryone(mMessageId)
        mAppDatabase!!.getChatListDao()
            .updateMessageForDeleteForEveryone(mMessageId, SharedPreferenceEditor.getData(USER_ID))
        EventBus.getDefault().post("messenger")
        EventBus.getDefault().post("message_update")
    }
    private val mMessageDeletedOnChatListener = Emitter.Listener {
        val mChatDeletedModel =
            GsonBuilder().create()
                .fromJson(it[0].toString(), ChatDeletedSocketModel::class.java)
        Timber.tag("Krish")
            .e("mMessageDeletedOnChatListener: ${Gson().toJson(mChatDeletedModel)}")
        // ackEvent(context, MESSAGE_DELETED_ON_GROUP_EVENT)
        val mMessageId = mChatDeletedModel.messageId
        mAppDatabase!!.getChatDao()
            .updateMessageForDeleteForEveryone(mMessageId)
        mAppDatabase!!.getChatListDao().updateMessageForDeleteForEveryone(
            mMessageId,
            SharedPreferenceEditor.getData(USER_ID)
        )
        EventBus.getDefault().post("messenger")
        EventBus.getDefault().post("message_update")
    }

    @SuppressLint("SimpleDateFormat")
    private val mGetGroupsListener = Emitter.Listener {
        Timber.tag("Krish")
            .e("mGetGroupsListener: ${Gson().toJson(it[0].toString())}")
        //        ackEvent(context, GET_GROUPS_EVENT)
        val mJsonArray = it[0] as JSONArray
        if (mJsonArray.length() != 0) {
            for (i in 0 until mJsonArray.length()) {
                val mJsonObject = mJsonArray.optJSONObject(i)
                val mGetGroupsModel = GsonBuilder().create()
                    .fromJson(mJsonObject.toString(), GetGroupsModel::class.java)
                val listGroup = mAppDatabase!!.getGroupDetailsDao()
                    .getGroupDetails(
                        mGetGroupsModel.group.groupId,
                        SharedPreferenceEditor.getData(USER_ID)
                    )
                if (listGroup.isNotEmpty()) {
                    mAppDatabase!!.getGroupDetailsDao().update(mGetGroupsModel.group)
                } else {
                    mAppDatabase!!.getGroupDetailsDao().insert(mGetGroupsModel.group)
                }
                //event to get Group members based on group id
                val mGroupMemberDetails = mAppDatabase!!.getGroupMemberDao()
                    .getGroupMembers(
                        mGetGroupsModel.group.groupId,
                        SharedPreferenceEditor.getData(USER_ID)
                    )
                if (mGroupMemberDetails.isEmpty()) {
                    val mGroupMemberSocketModel = GroupMembersSocketModel()
                    mGroupMemberSocketModel.sender = SharedPreferenceEditor.getData(USER_ID)
                    mGroupMemberSocketModel.groupId = mGetGroupsModel.group.groupId
                    val groupMembersJson = Gson().toJson(mGroupMemberSocketModel)
                    val jsonObject = JSONObject(groupMembersJson)
                    ChatApp.mSocketHelper?.getGroupMembersEvent(jsonObject)
                }
                val listGroupChat =
                    mAppDatabase!!.getChatDao().getGroupChat(
                        mGetGroupsModel.group.groupId,
                        SharedPreferenceEditor.getData(USER_ID)
                    )
                val mListMembersId = ArrayList<String>()
                if (listGroupChat.isEmpty()) {
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    val mCreatedDate = simpleDateFormat.parse(mGetGroupsModel.group.createdDate!!)
                    val chatModel = ChatModel()
                    chatModel.sender = mGetGroupsModel.group.createdBy
                    chatModel.receiver = mGetGroupsModel.group.groupId
                    chatModel.receivers = mListMembersId
                    chatModel.message = "created group"
                        .plus(" \"")
                        .plus(mGetGroupsModel.group.groupTitle)
                        .plus("\"")
                    chatModel.chatTime = mCreatedDate!!.time.toString()
                    chatModel.chatType = ChatTypes.GROUP.type
                    chatModel.checkForwarded = false
                    chatModel.checkReply = false
                    chatModel.messageId =
                        getMessageId(
                            getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(USER_ID))
                                .plus("-")
                                .plus(mGetGroupsModel.group.groupId)
                        )
                    chatModel.messageStatus = ChatMessageStatus.SENT.ordinal
                    chatModel.messageType = ChatMessageTypes.CREATEGROUP.type
                    mAppDatabase!!.getChatDao().insert(chatModel)
                    val chatListModel = ChatListModel()
                    chatListModel.chatType = ChatTypes.GROUP.type
                    chatListModel.userId = SharedPreferenceEditor.getData(USER_ID)
                    chatListModel.sender = chatModel.sender
                    chatListModel.receiverId = mGetGroupsModel.group.groupId
                    chatListModel.messageId = chatModel.messageId
                    if (chatModel.messageType == ChatMessageTypes.TEXT.type ||
                        infoMessageTypes().contains(chatModel.messageType)
                    ) {
                        chatListModel.lastMessage = chatModel.message
                    } else {
                        chatListModel.lastMessage = getLastMessage(chatModel.messageType)
                    }
                    chatListModel.messageType = chatModel.messageType
                    if (chatListModel.chatType == ChatTypes.SINGLE.type) {
                        val listMessageCount =
                            mAppDatabase!!.getChatDao().getMessageCount(
                                SharedPreferenceEditor.getData(USER_ID),
                                mGetGroupsModel.group.groupId
                            )
                        chatListModel.messageCount = listMessageCount.size
                    } else if (chatListModel.chatType == ChatTypes.GROUP.type) {
                        val listMessageCount =
                            mAppDatabase!!.getChatDao()
                                .getGroupMessageCount(
                                    SharedPreferenceEditor.getData(USER_ID),
                                    mGetGroupsModel.group.groupId,
                                    infoMessageTypes()
                                )
                        chatListModel.messageCount = listMessageCount.size
                    }
                    chatListModel.time = chatModel.chatTime
                    val userDetailsModel = mAppDatabase!!.getUserDetailsDao()
                        .getUserDetails(mGetGroupsModel.group.groupId)
                    if (userDetailsModel.isNotEmpty()) {
                        userDetailsModel[0].username?.let {
                            chatListModel.name = it
                        }
                        userDetailsModel[0].profilePicture?.let {
                            chatListModel.profilePicture = it
                        }
                    } else {
                        val mGroupDetails = mAppDatabase!!.getGroupDetailsDao()
                            .getGroupDetails(
                                mGetGroupsModel.group.groupId,
                                SharedPreferenceEditor.getData(USER_ID)
                            )
                        if (mGroupDetails.isNotEmpty()) {
                            chatListModel.name = mGroupDetails[0].groupTitle
                            chatListModel.profilePicture = mGroupDetails[0].groupImage
                        }
                    }
                    val mObjectId = SharedPreferenceEditor.getData(USER_ID).plus("-")
                        .plus(mGetGroupsModel.group.groupId)
                    chatListModel.objectId = getObjectId(mObjectId)

                    if (chatModel.checkEdit) {
                        val receiverId = chatModel.receiver
                        val listMessage = mAppDatabase!!.getChatListDao()
                            .isMessageExists(
                                SharedPreferenceEditor.getData(USER_ID),
                                receiverId,
                                chatModel.messageId
                            )
                        if (listMessage.isNotEmpty()) {
                            mAppDatabase!!.getChatListDao().update(chatListModel)
                        }
                    } else {
                        val listChat =
                            mAppDatabase!!.getChatListDao()
                                .getChatList(
                                    mGetGroupsModel.group.groupId,
                                    SharedPreferenceEditor.getData(USER_ID)
                                )
                        if (listChat.isNotEmpty()) {
                            mAppDatabase!!.getChatListDao().update(chatListModel)
                        } else {
                            mAppDatabase!!.getChatListDao().insert(chatListModel)
                        }
                    }
                }
            }
            EventBus.getDefault().post("message_update")
        }
    }
    private val mAudioPlayedListener = Emitter.Listener {
        Timber.tag("Krish")
            .e("mAudioPlayedListener: ${Gson().toJson(it[0].toString())}")
        //        ackEvent(context, AUDIO_PLAYED_ON_CHAT_EVENT)
        val mAudioMessageModel =
            GsonBuilder().create().fromJson(it[0].toString(), AudioMessage::class.java)
        mAppDatabase!!.getAudioStatusDao().updateAudioStatus(
            mAudioMessageModel.messageId,
            SharedPreferenceEditor.getData(USER_ID)
        )
        EventBus.getDefault().post(mAudioMessageModel)
    }
    private val mAudioPlayedInGroupListener = Emitter.Listener {
        Timber.tag("Krish")
            .e("mAudioPlayedInGroupListener: ${Gson().toJson(it[0].toString())}")
        //        ackEvent(context, AUDIO_PLAYED_ON_GROUP_CHAT_EVENT)
        val mGroupChatAudioPlayedModel =
            GsonBuilder().create().fromJson(it[0].toString(), GroupChatAudioPlayedModel::class.java)
        val mGroupChatMessagelist =
            mAppDatabase!!.getChatDao()
                .getGroupChatData(
                    mGroupChatAudioPlayedModel.messageId,
                    SharedPreferenceEditor.getData(USER_ID)
                )
        if (mGroupChatAudioPlayedModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
            val mListAudioStatus = mAppDatabase!!.getAudioStatusDao()
                .getAudioMessage(
                    mGroupChatAudioPlayedModel.messageId,
                    SharedPreferenceEditor.getData(USER_ID)
                )
            if (mListAudioStatus.isNotEmpty()) {
                for (i in mListAudioStatus.indices) {
                    if (!mListAudioStatus[i].receivers.contains(mGroupChatAudioPlayedModel.sender)) {
                        mListAudioStatus[i].receivers.add(mGroupChatAudioPlayedModel.sender)
                        mListAudioStatus[i].checkPlayed = true
                        mAppDatabase!!.getAudioStatusDao().update(mListAudioStatus[i])
                    }
                }
            } else {
                val mGroupMembersId = ArrayList<String>()
                mGroupMembersId.add(mGroupChatAudioPlayedModel.sender)
                val mAudioStatus = AudioStatus()
                mAudioStatus.userId = SharedPreferenceEditor.getData(USER_ID)
                mAudioStatus.messageId = mGroupChatAudioPlayedModel.messageId
                mAudioStatus.checkPlayed = true
                mAudioStatus.receivers = mGroupMembersId
                mAppDatabase!!.getAudioStatusDao().insert(mAudioStatus)
            }
        } else {
            val mListAudioStatus = mAppDatabase!!.getAudioStatusDao()
                .getAudioMessage(
                    mGroupChatAudioPlayedModel.messageId,
                    SharedPreferenceEditor.getData(USER_ID)
                )
            if (mListAudioStatus.isNotEmpty()) {
                for (i in mListAudioStatus.indices) {
                    if (!mListAudioStatus[i].receivers.contains(mGroupChatAudioPlayedModel.sender)) {
                        mListAudioStatus[i].receivers.add(mGroupChatAudioPlayedModel.sender)
                        var mPlayedByEveryone = true
                        if (mGroupChatAudioPlayedModel.receivers.isNotEmpty()) {
                            for (j in mGroupChatAudioPlayedModel.receivers.indices) {
                                if (!mListAudioStatus[i].receivers.contains(
                                        mGroupChatAudioPlayedModel.receivers[j]
                                    ) &&
                                    mGroupChatAudioPlayedModel.receivers[j] != SharedPreferenceEditor.getData(
                                        USER_ID
                                    ) &&
                                    mGroupChatMessagelist.isNotEmpty() && mGroupChatAudioPlayedModel.receivers[j] != mGroupChatMessagelist[0].sender
                                ) {
                                    mPlayedByEveryone = false
                                }
                            }
                        }
                        mListAudioStatus[i].checkPlayed = mPlayedByEveryone
                        mAppDatabase!!.getAudioStatusDao().update(mListAudioStatus[i])
                    }
                }
            } else {
                val mGroupMembersId = ArrayList<String>()
                val mListGroupMembers =
                    mAppDatabase!!.getGroupMemberDao()
                        .getGroupMembers(
                            mGroupChatAudioPlayedModel.groupId,
                            SharedPreferenceEditor.getData(USER_ID)
                        )
                if (mListGroupMembers.isNotEmpty()) {
                    for (i in mListGroupMembers.indices) {
                        mGroupMembersId.add(mListGroupMembers[i].memberId)
                    }
                }
                val mListenedMembersId = ArrayList<String>()
                mListenedMembersId.add(mGroupChatAudioPlayedModel.sender)
                val mAudioStatus = AudioStatus()
                mAudioStatus.userId = SharedPreferenceEditor.getData(USER_ID)
                mAudioStatus.messageId = mGroupChatAudioPlayedModel.messageId
                mAudioStatus.checkPlayed = mGroupMembersId.size == 1
                mAudioStatus.receivers = mListenedMembersId
                mAppDatabase!!.getAudioStatusDao().insert(mAudioStatus)
            }
        }
        EventBus.getDefault().post(mGroupChatAudioPlayedModel)
    }
    private val mErrorListener = Emitter.Listener {
        Timber.tag("Krish")
            .e("mErrorListener: ${Gson().toJson(it[0].toString())}")
        //        ackEvent(context, ERROR_EVENT)
    }
    private val mGetGroupMembersListener = Emitter.Listener {
        Timber.tag("Krish")
            .e("mGetGroupMembersListener: ${Gson().toJson(it[0].toString())}")
        //        ackEvent(context, GET_GROUP_MEMBERS_EVENT)
        val mJsonArray = it[0] as JSONArray
        if (mJsonArray.length() != 0) {
            val mNewFriendsList = ArrayList<String>()
            var mGroupId: String = ""
            for (i in 0 until mJsonArray.length()) {
                val mJsonObject = mJsonArray.optJSONObject(i)
                val mGetGroupMembersModel =
                    GsonBuilder().create()
                        .fromJson(mJsonObject.toString(), GetGroupMembersModel::class.java)
                mGroupId = mGetGroupMembersModel.groupId.groupId
                val mMemberId = mGetGroupMembersModel.memberId
                val mGroupMemberModel = GroupMemberModel()
                mGroupMemberModel.checkAdmin = mGetGroupMembersModel.checkAdmin
                mGroupMemberModel.checkRemoved = false
                mGroupMemberModel.groupId = mGroupId
                mGroupMemberModel.memberId = mGetGroupMembersModel.memberId
                mGroupMemberModel.objectId =
                    getObjectId(mGroupId.plus("-").plus(mGetGroupMembersModel.memberId))
                val listMembersModel =
                    mAppDatabase!!.getGroupMemberDao()
                        .getGroupMemberDetails(
                            mGroupId,
                            mMemberId,
                            SharedPreferenceEditor.getData(USER_ID)
                        )
                if (listMembersModel.isEmpty()) {
                    mAppDatabase!!.getGroupMemberDao().insert(mGroupMemberModel)
                } else {
                    mAppDatabase!!.getGroupMemberDao().update(mGroupMemberModel)
                }
                if (mAppDatabase!!.getUserDetailsDao()
                        .getUserDetails(mGetGroupMembersModel.memberId)
                        .isEmpty()
                ) {
                    mNewFriendsList.add(mGetGroupMembersModel.memberId)
                }
            }
            if (mNewFriendsList.isNotEmpty()) {
                val mAddFriendsList = ArrayList<AddFriendUserModel.DataModel>()
                for (i in 0 until mNewFriendsList.size) {
                    val mDataModel = AddFriendUserModel()
                        .DataModel()
                    mDataModel.id = mNewFriendsList[i]
                    mAddFriendsList.add(mDataModel)
                }
                val mAddFriendUserModel =
                    AddFriendUserModel()
                mAddFriendUserModel.data = mAddFriendsList
                //                EventBus.getDefault().post(mAddFriendUserModel)
            }
            EventBus.getDefault().post("messenger")
            EventBus.getDefault().post(mGroupId)
        }
    }
    private val mGetGroupInfoListener = Emitter.Listener {
        Log.e("TAG", "mGetGroupInfoListener: " + Gson().toJson(it[0].toString()))
        val mGroupDetailModel =
            GsonBuilder().create().fromJson(it[0].toString(), GroupDetailsModel::class.java)
        val listGroupDetails =
            mAppDatabase!!.getGroupDetailsDao()
                .getGroupDetails(mGroupDetailModel.groupId, SharedPreferenceEditor.getData(USER_ID))
        if (listGroupDetails.isNotEmpty()) {
            mAppDatabase!!.getGroupDetailsDao().update(listGroupDetails[0])
        } else {
            mAppDatabase!!.getGroupDetailsDao().insert(mGroupDetailModel)
        }
        EventBus.getDefault().post("message_update")
    }
    private val mBlockUsersListener = Emitter.Listener {
        Log.e("TAG", "mBlockUsersListener: " + Gson().toJson(it[0].toString()))
        val mBlockUserSocketModel =
            GsonBuilder().create().fromJson(it[0].toString(), BlockUserSocketModel::class.java)
        var mReceiverId: String = ""
        mReceiverId = if (mBlockUserSocketModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
            mBlockUserSocketModel.blockUser!!
        } else {
            mBlockUserSocketModel.sender
        }
        val mBlockedUsersList =
            mAppDatabase!!.getBlockedUsersDao()
                .getBlockedUsers(mReceiverId, SharedPreferenceEditor.getData(USER_ID))
        if (mBlockedUsersList.isNotEmpty()) {
            if (mBlockUserSocketModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
                mBlockedUsersList[0].checkBlockedByYou = true
                mAppDatabase!!.getBlockedUsersDao().update(mBlockedUsersList[0])
                EventBus.getDefault().post(mBlockedUsersList[0])
            } else {
                mBlockedUsersList[0].checkBlockedByUser = true
                mAppDatabase!!.getBlockedUsersDao().update(mBlockedUsersList[0])
                EventBus.getDefault().post(mBlockedUsersList[0])
            }
        } else {
            if (mBlockUserSocketModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
                val mObjectId = SharedPreferenceEditor.getData(USER_ID).plus("-").plus(mReceiverId)
                val mBlockedUsersModel = BlockedUsersModel()
                mBlockedUsersModel.userId = mBlockUserSocketModel.sender
                mBlockedUsersModel.friendId = mReceiverId
                mBlockedUsersModel.objectId = getObjectId(mObjectId)
                mBlockedUsersModel.checkBlockedByYou = true
                mBlockedUsersModel.checkBlockedByUser = false
                mAppDatabase!!.getBlockedUsersDao().insert(mBlockedUsersModel)
                EventBus.getDefault().post(mBlockedUsersModel)
            } else {
                val mObjectId = SharedPreferenceEditor.getData(USER_ID).plus("-").plus(mReceiverId)
                val mBlockedUsersModel = BlockedUsersModel()
                mBlockedUsersModel.userId = SharedPreferenceEditor.getData(USER_ID)
                mBlockedUsersModel.friendId = mReceiverId
                mBlockedUsersModel.objectId = getObjectId(mObjectId)
                mBlockedUsersModel.checkBlockedByYou = false
                mBlockedUsersModel.checkBlockedByUser = true
                mAppDatabase!!.getBlockedUsersDao().insert(mBlockedUsersModel)
                EventBus.getDefault().post(mBlockedUsersModel)
            }
        }
    }
    private val mUnblockUsersListener = Emitter.Listener {
        Log.e("TAG", "mUnblockUsersListener: " + Gson().toJson(it[0].toString()))
        val mBlockUserSocketModel =
            GsonBuilder().create().fromJson(it[0].toString(), BlockUserSocketModel::class.java)
        var mReceiverId: String = ""
        mReceiverId = if (mBlockUserSocketModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
            mBlockUserSocketModel.unblockUser!!
        } else {
            mBlockUserSocketModel.sender
        }
        val mBlockedUsersList =
            mAppDatabase!!.getBlockedUsersDao()
                .getBlockedUsers(mReceiverId, SharedPreferenceEditor.getData(USER_ID))
        if (mBlockedUsersList.isNotEmpty()) {
            if (mBlockUserSocketModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
                mBlockedUsersList[0].checkBlockedByYou = false
                mAppDatabase!!.getBlockedUsersDao().update(mBlockedUsersList[0])
                EventBus.getDefault().post(mBlockedUsersList[0])
            } else {
                mBlockedUsersList[0].checkBlockedByUser = false
                mAppDatabase!!.getBlockedUsersDao().update(mBlockedUsersList[0])
                EventBus.getDefault().post(mBlockedUsersList[0])
            }
        }
    }
    private val mBlockedUsersListener = Emitter.Listener {
        Log.e("TAG", "mBlockedUsersListener: " + Gson().toJson(it[0].toString()))
        val mGetBlockedContactsModel =
            GsonBuilder().create().fromJson(it[0].toString(), GetBlockedContactsModel::class.java)
        val mSenderIdList = mGetBlockedContactsModel.sender
        val mReceiversList = mGetBlockedContactsModel.receivers
        val mReceiversIdList = ArrayList<String>()
        if (mReceiversList.isNotEmpty()) {
            for (i in 0 until mReceiversList.size) {
                mReceiversIdList.add(mReceiversList[i].id)
            }
        }
        if (mSenderIdList.isNotEmpty()) {
            for (i in 0 until mSenderIdList.size) {
                val mReceiverId = mSenderIdList[i]
                val mBlockedUsersList =
                    mAppDatabase!!.getBlockedUsersDao()
                        .getBlockedUsers(mReceiverId, SharedPreferenceEditor.getData(USER_ID))
                if (mBlockedUsersList.isNotEmpty()) {
                    mBlockedUsersList[0].checkBlockedByYou = true
                    mAppDatabase!!.getBlockedUsersDao().update(mBlockedUsersList[0])
                    EventBus.getDefault().post(mBlockedUsersList[0])
                } else {
                    val mObjectId =
                        SharedPreferenceEditor.getData(USER_ID).plus("-").plus(mReceiverId)
                    val mBlockedUsersModel = BlockedUsersModel()
                    mBlockedUsersModel.userId = SharedPreferenceEditor.getData(USER_ID)
                    mBlockedUsersModel.friendId = mReceiverId
                    mBlockedUsersModel.objectId = getObjectId(mObjectId)
                    mBlockedUsersModel.checkBlockedByYou = true
                    mBlockedUsersModel.checkBlockedByUser = false
                    mAppDatabase!!.getBlockedUsersDao().insert(mBlockedUsersModel)
                    EventBus.getDefault().post(mBlockedUsersModel)
                }
            }
        }
        if (mReceiversIdList.isNotEmpty()) {
            for (i in 0 until mReceiversIdList.size) {
                val mReceiverId = mReceiversIdList[i]
                val mBlockedUsersList =
                    mAppDatabase!!.getBlockedUsersDao()
                        .getBlockedUsers(mReceiverId, SharedPreferenceEditor.getData(USER_ID))
                if (mBlockedUsersList.isNotEmpty()) {
                    mBlockedUsersList[0].checkBlockedByUser = true
                    mAppDatabase!!.getBlockedUsersDao().update(mBlockedUsersList[0])
                    EventBus.getDefault().post(mBlockedUsersList[0])
                } else {
                    val mObjectId =
                        SharedPreferenceEditor.getData(USER_ID).plus("-").plus(mReceiverId)
                    val mBlockedUsersModel = BlockedUsersModel()
                    mBlockedUsersModel.userId = SharedPreferenceEditor.getData(USER_ID)
                    mBlockedUsersModel.friendId = mReceiverId
                    mBlockedUsersModel.objectId = getObjectId(mObjectId)
                    mBlockedUsersModel.checkBlockedByYou = false
                    mBlockedUsersModel.checkBlockedByUser = true
                    mAppDatabase!!.getBlockedUsersDao().insert(mBlockedUsersModel)
                    EventBus.getDefault().post(mBlockedUsersModel)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun connectSocket(userId: String, authToken: String) {
        try {
            SharedPreferenceEditor.saveData(USER_ID, userId)
            val mOptions = IO.Options()
            Log.e("Nive ", "connectSocket: ${userId}")
            mOptions.query =
                "socketVersion=" + "v1.0.0" +
                        "&id=" + userId

            mSocket = IO.socket(
                BuildConfig.SOCKET_URL,
                mOptions
            )

            mSocket.on(Socket.EVENT_CONNECT, onConnect)
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
//            mSocket.on(Socket.CONNECT_TIM, onConnectTimeout)
            mSocket.on(SINGLE_CHAT_SEND_MESSAGE, mSingleChatSendMessageEvent)
            mSocket.on(SINGLE_CHAT_TYPING, mSingleChatTyping)
            mSocket.on(SINGLE_CHAT_DELIVERED, mSingleChatDelivered)
            mSocket.on(SINGLE_CHAT_READ, mSingleChatRead)
            mSocket.on(ADD_CONTACT, mAddContact)
            mSocket.on(PROFILE_UPDATED, mProfileUpdate)
            mSocket.on(GET_MY_CONTACTS, mGetMyContacts)
            mSocket.on(ONLINE_OFFLINE, mContactLastSeen)
            mSocket.on(ADDED_IN_NEW_GROUP_EVENT, mAddedInNewGroup)
            mSocket.on(GROUP_CHAT_MESSAGE_EVENT, mGroupChatMessage)
            mSocket.on(GROUP_INFO_UPDATE_EVENT, mGroupInfoListener)
            mSocket.on(GROUP_ADD_MEMBER_EVENT, mGroupAddMemberListener)
            mSocket.on(GROUP_REMOVE_MEMBER_EVENT, mGroupRemoveMemberListener)
            mSocket.on(GROUP_EXIT_MEMBER_EVENT, mExitGroupListener)
            mSocket.on(GROUP_ADD_ADMIN_EVENT, mAddAdminListener)
            mSocket.on(GROUP_REMOVE_ADMIN_EVENT, mRemoveAdminListener)
            mSocket.on(GROUP_CHAT_DELIVERED_EVENT, mGroupChatDeliveredListener)
            mSocket.on(GROUP_CHAT_READ_EVENT, mGroupChatReadListener)
            mSocket.on(REPORT_MESSAGE_EVENT, mReportMessageListener)
            mSocket.on(GET_USERS_INFO_EVENT, mGetUserInfoListener)
            mSocket.on(MESSAGE_DELETED_ON_GROUP_EVENT, mMessageDeletedOnGroupListener)
            mSocket.on(MESSAGE_DELETED_ON_CHAT_EVENT, mMessageDeletedOnChatListener)
            mSocket.on(GET_GROUPS_EVENT, mGetGroupsListener)
            mSocket.on(AUDIO_PLAYED_ON_CHAT_EVENT, mAudioPlayedListener)
            mSocket.on(AUDIO_PLAYED_ON_GROUP_CHAT_EVENT, mAudioPlayedInGroupListener)
            mSocket.on(ERROR_EVENT, mErrorListener)
            mSocket.on(GET_GROUP_MEMBERS_EVENT, mGetGroupMembersListener)
            mSocket.on(GET_GROUP_INFO_EVENT, mGetGroupInfoListener)
            mSocket.on(BLOCK_CONTACT_EVENT, mBlockUsersListener)
            mSocket.on(UN_BLOCK_CONTACT_EVENT, mUnblockUsersListener)
            mSocket.on(GET_BLOCKED_CONTACTS_EVENT, mBlockedUsersListener)
            mSocket.connect()
        } catch (e: URISyntaxException) {
        }
    }

    fun getSocketInstance(): Socket {
        return mSocket
    }

    fun sendSingleChat(jsonObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(SINGLE_CHAT_SEND_MESSAGE, jsonObject)
            }
        } catch (e: Exception) {
        }
    }

    fun sendSingleChatTyping(singleChatTypingObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(SINGLE_CHAT_TYPING, singleChatTypingObject)
            }
        } catch (e: Exception) {
            Log.e("Nive ", "sendSingleChatTyping:Exception ${e.message}")
        }
    }

    private fun sendSingleChatDelivered(singleChatDeliveredObject: JSONObject) {
        if (mSocket.connected()) {
            mSocket.emit(SINGLE_CHAT_DELIVERED, singleChatDeliveredObject)
        }
    }

    private fun sendSingleChatRead(singleChatReadObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(SINGLE_CHAT_READ, singleChatReadObject)
            }
        } catch (e: Exception) {
            Log.e("Nive ", "sendSingleChatRead: ${e.message}")
        }
    }

    fun sendAddContact(addContactObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(ADD_CONTACT, addContactObject)
            }
        } catch (e: Exception) {
        }
    }

    private fun onlineOfflineSocket(context: Context) {
        val jsonObject = JSONObject()
        jsonObject.put("id", SharedPreferenceEditor.getData(USER_ID))
        jsonObject.put("status", "ONLINE")
        sendOnlineOffline(jsonObject)
    }

    fun sendOnlineOffline(objectOnlineOffline: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(ONLINE_OFFLINE, objectOnlineOffline)
            }
        } catch (e: Exception) {
        }
    }

    private fun getFriendlist(context: Context) {
        val jsonObject = JSONObject()
        jsonObject.put("id", SharedPreferenceEditor.getData(USER_ID))
        getMyContacts(jsonObject)
    }

    private fun getMyContacts(objectGetMyContacts: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GET_MY_CONTACTS, objectGetMyContacts)
            }
        } catch (e: Exception) {
        }
    }

    fun audioPlayed(objectAudioPlayed: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(AUDIO_PLAYED_ON_CHAT_EVENT, objectAudioPlayed)
            }
        } catch (e: Exception) {
        }
    }

    fun audioPlayedOnGroup(objectAudioPlayedOnGroup: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(AUDIO_PLAYED_ON_GROUP_CHAT_EVENT, objectAudioPlayedOnGroup)
            }
        } catch (e: Exception) {
        }
    }

    fun blockUser(mBlockUserObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(BLOCK_CONTACT_EVENT, mBlockUserObject)
            }
        } catch (e: Exception) {
        }
    }

    fun unblockUser(mUnblockUserObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(UN_BLOCK_CONTACT_EVENT, mUnblockUserObject)
            }
        } catch (e: Exception) {
        }
    }

    fun getBlockedUsers(mBlockedUserObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GET_BLOCKED_CONTACTS_EVENT, mBlockedUserObject)
            }
        } catch (e: Exception) {
        }
    }

    private fun getBlockedContactsJson(context: Context) {
        val jsonObject = JSONObject()
        jsonObject.put("sender", SharedPreferenceEditor.getData(USER_ID))
        getBlockedContacts(jsonObject)
    }

    private fun getBlockedContacts(objectGetBlockedContacts: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GET_BLOCKED_CONTACTS_EVENT, objectGetBlockedContacts)
            }
        } catch (e: Exception) {
        }
    }

    /**
     * EVENTS for Group
     * */
    fun createGroupChat(jsonObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                Log.e("TAG", "createGroupChat: " + Gson().toJson(jsonObject))
                mSocket.emit(CREATE_GROUP_EVENT, jsonObject)
            }
        } catch (e: Exception) {
        }
    }

    private fun getUserInfo(mUserInfoObject: JSONObject) {
        if (mSocket.connected()) {
            mSocket.emit(GET_USERS_INFO_EVENT, mUserInfoObject)
        }
    }

    fun sendGroupChat(mGroupChatObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GROUP_CHAT_MESSAGE_EVENT, mGroupChatObject)
            }
        } catch (e: Exception) {
        }
    }

    fun updateGroupInfo(mGroupInfoObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GROUP_INFO_UPDATE_EVENT, mGroupInfoObject)
            }
        } catch (e: Exception) {
        }
    }

    fun addGroupMember(mGroupAddMemberObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GROUP_ADD_MEMBER_EVENT, mGroupAddMemberObject)
            }
        } catch (e: Exception) {
        }
    }

    fun removeGroupMember(mGroupRemoveMemberObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GROUP_REMOVE_MEMBER_EVENT, mGroupRemoveMemberObject)
            }
        } catch (e: Exception) {
        }
    }

    fun exitGroup(mExitGroupObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GROUP_EXIT_MEMBER_EVENT, mExitGroupObject)
            }
        } catch (e: Exception) {
        }
    }

    fun makeGroupAdmin(mAddAdminObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GROUP_ADD_ADMIN_EVENT, mAddAdminObject)
            }
        } catch (e: Exception) {
        }
    }

    fun removeGroupAdmin(mRemoveAdminObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GROUP_REMOVE_ADMIN_EVENT, mRemoveAdminObject)
            }
        } catch (e: Exception) {
        }
    }

    private fun groupChatDeliver(mGroupChatDeliverObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GROUP_CHAT_DELIVERED_EVENT, mGroupChatDeliverObject)
            }
        } catch (e: Exception) {
        }
    }

    private fun groupChatRead(mGroupChatReadObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GROUP_CHAT_READ_EVENT, mGroupChatReadObject)
            }
        } catch (e: Exception) {
        }
    }

    fun reportMessage(mReportMessageObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(REPORT_MESSAGE_EVENT, mReportMessageObject)
            }
        } catch (e: Exception) {
        }
    }

    fun deleteMessageForEveryone(mDeleteMessageObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(DELETE_FOR_EVERYONE_CHAT_EVENT, mDeleteMessageObject)
            }
        } catch (e: Exception) {
        }
    }

    private fun getGroups(context: Context) {
        val jsonObject = JSONObject()
        jsonObject.put("member_id", SharedPreferenceEditor.getData(USER_ID))
        getGroupsEvent(jsonObject)
    }

    private fun getGroupsEvent(objectGetMyGroups: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GET_GROUPS_EVENT, objectGetMyGroups)
            }
        } catch (e: Exception) {
        }
    }

    private fun getGroupMembersEvent(objectGetGroupMembers: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GET_GROUP_MEMBERS_EVENT, objectGetGroupMembers)
            }
        } catch (e: Exception) {
        }
    }

    fun getGroupInfoEvent(objectGetGroupInfo: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(GET_GROUP_INFO_EVENT, objectGetGroupInfo)
            }
        } catch (e: Exception) {
        }
    }

    fun deleteMessageForEveryoneGroup(mDeleteMessageObject: JSONObject) {
        try {
            if (mSocket.connected()) {
                mSocket.emit(DELETE_FOR_EVERYONE_GROUP_EVENT, mDeleteMessageObject)
            }
        } catch (e: Exception) {
        }
    }

    private fun startWorker(
        context: Context,
        uri: String,
        messageType: String,
        messageId: String,
        mReceiverId: String
    ) {
        val data = Data.Builder()
            .putString("uri", uri)
            .putString("messageType", messageType)
            .putString("messageId", messageId)
            .putString("receiverId", mReceiverId)
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
        val oneTimeRequest = OneTimeWorkRequest
            .Builder(DownloadWorker::class.java)
            .setInputData(data)
            .addTag("demo")
            .setConstraints(constraints.build()) // i added constraints
            .build()
        WorkManager.getInstance(context).enqueue((oneTimeRequest))
    }

    private fun lastMessageUpdate(context: Context, chatModel: ChatModel) {
        var mReceiverId: String = ""
        val chatListModel = ChatListModel()
        chatListModel.chatType = ChatTypes.SINGLE.type
        chatListModel.userId = SharedPreferenceEditor.getData(USER_ID)
        mReceiverId = if (chatModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
            chatModel.receiver
        } else {
            chatModel.sender
        }
        chatListModel.receiverId = mReceiverId
        chatListModel.messageId = chatModel.messageId
        if (chatModel.messageType == ChatMessageTypes.TEXT.type) {
            chatListModel.lastMessage = chatModel.message
        } else {
            chatListModel.lastMessage = getLastMessage(chatModel.messageType)
        }
        chatListModel.messageType = chatModel.messageType
        val listMessageCount =
            mAppDatabase!!.getChatDao()
                .getMessageCount(SharedPreferenceEditor.getData(USER_ID), mReceiverId)
        chatListModel.messageCount = listMessageCount.size
        chatListModel.time = chatModel.chatTime
        val userDetailsModel = mAppDatabase!!.getUserDetailsDao().getUserDetails(mReceiverId)
        if (userDetailsModel.isNotEmpty()) {
            userDetailsModel[0].username?.let {
                chatListModel.name = it
            }
            userDetailsModel[0].profilePicture?.let {
                chatListModel.profilePicture = it
            }
        }
        val mObjectId = SharedPreferenceEditor.getData(USER_ID).plus("-").plus(mReceiverId)
        chatListModel.objectId = getObjectId(mObjectId)

        if (chatModel.checkEdit) {
            val listMessage = mAppDatabase!!.getChatListDao()
                .isMessageExists(
                    SharedPreferenceEditor.getData(USER_ID),
                    mReceiverId,
                    chatModel.messageId
                )
            if (listMessage.isNotEmpty()) {
                mAppDatabase!!.getChatListDao().update(chatListModel)
            }
        } else {
            val listChat =
                mAppDatabase!!.getChatListDao()
                    .getChatList(mReceiverId, SharedPreferenceEditor.getData(USER_ID))
            if (listChat.isNotEmpty()) {
                mAppDatabase!!.getChatListDao().update(chatListModel)
            } else {
                mAppDatabase!!.getChatListDao().insert(chatListModel)
            }
        }
        if (chatModel.receiver == SharedPreferenceEditor.getData(USER_ID)) {
            if (chatModel.messageStatus != ChatMessageStatus.DELIVERED.ordinal) {
                val chatDelieverModel = ChatDelieverModel()
                chatDelieverModel.sender = chatModel.receiver
                chatDelieverModel.receiver = chatModel.sender
                chatDelieverModel.messageId = chatModel.messageId
                val chatDelieverJson = Gson().toJson(chatDelieverModel)
                val jsonObjectDeliever = JSONObject(chatDelieverJson)
                ChatApp.mSocketHelper?.sendSingleChatDelivered(jsonObjectDeliever)
            }
        }
        EventBus.getDefault().post(chatModel)
        EventBus.getDefault().post("message_update")

        if (chatModel.messageType != ChatMessageTypes.TEXT.type) {
            if (chatModel.messageType ==
                ChatMessageTypes.IMAGE.type
            ) {
                if (chatModel.sender != SharedPreferenceEditor.getData(USER_ID)) {
                    if (null == BaseUtils.loadImagePath(chatModel.messageId)) {
                        Log.e("Nive ", "lastMessageUpdate: worker ")
                        startWorker(
                            context, chatModel.uri!!,
                            chatModel.messageType,
                            chatModel.messageId,
                            mReceiverId
                        )
                    }
                }
            } else if (chatModel.messageType == ChatMessageTypes.AUDIO.type) {
                if (chatModel.sender != SharedPreferenceEditor.getData(USER_ID)) {
                    if (null == BaseUtils.loadAudioPath(
                            context, chatModel.messageId, 2
                        )
                    ) {
                        startWorker(
                            context, chatModel.uri!!,
                            chatModel.messageType,
                            chatModel.messageId,
                            mReceiverId
                        )
                    }
                }
            } else if (chatModel.messageType == ChatMessageTypes.VIDEO.type) {
                if (chatModel.sender != SharedPreferenceEditor.getData(USER_ID)) {
                    val videoDirectoryPath =
                        Environment.getExternalStorageDirectory().toString().plus("/")
                            .plus(Environment.DIRECTORY_PICTURES).plus("/")
                            .plus("Bee Bush Messenger Videos").plus("/")
                            .plus(chatModel.messageId)
                            .plus(".mp4")
                    if (!fileExist(videoDirectoryPath)) {
                        startWorker(
                            context, chatModel.uri!!,
                            chatModel.messageType,
                            chatModel.messageId,
                            mReceiverId
                        )
                    }
                }
            } else if (chatModel.messageType == ChatMessageTypes.DOCUMENT.type) {
                if (chatModel.sender != SharedPreferenceEditor.getData(USER_ID)) {
                    if (!fileExist(sDocumentDirectoryPath + "/" + chatModel.messageId + ".pdf")) {
                    }
                }
            }
        }
    }

    fun updateReadStatus(context: Context, mToId: String) {
        val listMessageId = ArrayList<String>()
        val listChat = mAppDatabase!!.getChatDao()
            .getMessageCount(SharedPreferenceEditor.getData(USER_ID), mToId)
        if (listChat.isNotEmpty()) {
            listMessageId.clear()
            for (i in listChat.indices) {
                listMessageId.add(listChat[i].messageId)
            }
            val chatReadModel = ChatReadModel()
            chatReadModel.sender = SharedPreferenceEditor.getData(USER_ID)
            chatReadModel.receiver = mToId
            chatReadModel.messageId = listMessageId
            val chatReadJson = Gson().toJson(chatReadModel)
            val jsonObjectRead = JSONObject(chatReadJson)
            ChatApp.mSocketHelper?.sendSingleChatRead(jsonObjectRead)
            val listChatRead =
                mAppDatabase!!.getChatDao()
                    .getMessageCount(SharedPreferenceEditor.getData(USER_ID), mToId)
            for (i in listChatRead.indices) {
                listChatRead[i].messageStatus = ChatMessageStatus.READ.ordinal
                mAppDatabase!!.getChatDao().update(listChatRead[i])
            }
            val listChatRecent =
                mAppDatabase!!.getChatListDao()
                    .getChatList(mToId, SharedPreferenceEditor.getData(USER_ID))
            for (i in listChatRecent.indices) {
                val listCount = mAppDatabase!!.getChatDao()
                    .getMessageCount(
                        SharedPreferenceEditor.getData(USER_ID),
                        listChatRecent[i].receiverId
                    )
                listChatRecent[i].messageCount = listCount.size
                mAppDatabase!!.getChatListDao().update(listChatRecent[i])
            }
            EventBus.getDefault().post("message_update")
        }
    }

    private fun lastGroupMessageUpdate(context: Context, chatModel: ChatModel) {
        var mReceiverId: String = ""
        val chatListModel = ChatListModel()
        chatListModel.chatType = ChatTypes.GROUP.type
        chatListModel.userId = SharedPreferenceEditor.getData(USER_ID)
        chatListModel.sender = chatModel.sender
        mReceiverId = chatModel.receiver
        chatListModel.receiverId = mReceiverId
        chatListModel.messageId = chatModel.messageId
        if (chatModel.messageType == ChatMessageTypes.TEXT.type ||
            infoMessageTypes().contains(chatModel.messageType)
        ) {
            chatListModel.lastMessage = chatModel.message
        } else {
            chatListModel.lastMessage = getLastMessage(chatModel.messageType)
        }
        chatListModel.messageType = chatModel.messageType
        if (chatListModel.chatType == ChatTypes.SINGLE.type) {
            val listMessageCount =
                mAppDatabase!!.getChatDao()
                    .getMessageCount(SharedPreferenceEditor.getData(USER_ID), mReceiverId)
            chatListModel.messageCount = listMessageCount.size
        } else if (chatListModel.chatType == ChatTypes.GROUP.type) {
            val listMessageCount =
                mAppDatabase!!.getChatDao()
                    .getGroupMessageCount(
                        SharedPreferenceEditor.getData(USER_ID),
                        mReceiverId,
                        infoMessageTypes()
                    )
            chatListModel.messageCount = listMessageCount.size
        }
        chatListModel.time = chatModel.chatTime
        val userDetailsModel = mAppDatabase!!.getUserDetailsDao().getUserDetails(mReceiverId)
        if (userDetailsModel.isNotEmpty()) {
            userDetailsModel[0].username?.let {
                chatListModel.name = it
            }
            userDetailsModel[0].profilePicture?.let {
                chatListModel.profilePicture = it

            }
        } else {
            val mGroupDetails = mAppDatabase!!.getGroupDetailsDao()
                .getGroupDetails(mReceiverId, SharedPreferenceEditor.getData(USER_ID))
            if (mGroupDetails.isNotEmpty()) {
                chatListModel.name = mGroupDetails[0].groupTitle
                chatListModel.profilePicture = mGroupDetails[0].groupImage
            }
        }
        val mObjectId = SharedPreferenceEditor.getData(USER_ID).plus("-").plus(mReceiverId)
        chatListModel.objectId = getObjectId(mObjectId)

        if (chatModel.checkEdit) {
            val listMessage = mAppDatabase!!.getChatListDao()
                .isMessageExists(
                    SharedPreferenceEditor.getData(USER_ID),
                    mReceiverId,
                    chatModel.messageId
                )
            if (listMessage.isNotEmpty()) {
                mAppDatabase!!.getChatListDao().update(chatListModel)
            }
        } else {
            val listChat =
                mAppDatabase!!.getChatListDao()
                    .getChatList(mReceiverId, SharedPreferenceEditor.getData(USER_ID))
            if (listChat.isNotEmpty()) {
                mAppDatabase!!.getChatListDao().update(chatListModel)
            } else {
                mAppDatabase!!.getChatListDao().insert(chatListModel)
            }
        }
        if (chatModel.sender != SharedPreferenceEditor.getData(USER_ID) &&
            chatModel.messageType != ChatMessageTypes.DATE.type &&
            !getInfoMessageTypes(chatModel.messageType)
        ) {
            if (chatModel.messageStatus != ChatMessageStatus.DELIVERED.ordinal) {
                val mGroupChatDelieverModel = GroupChatDeliveredModel()
                mGroupChatDelieverModel.sender = SharedPreferenceEditor.getData(USER_ID)
                mGroupChatDelieverModel.receivers = chatModel.receivers!!
                mGroupChatDelieverModel.messageId = chatModel.messageId
                mGroupChatDelieverModel.groupId = chatModel.receiver
                val mGroupChatDelieverJson = Gson().toJson(mGroupChatDelieverModel)
                val mGroupChatDeliverJson = JSONObject(mGroupChatDelieverJson)
                ChatApp.mSocketHelper?.groupChatDeliver(mGroupChatDeliverJson)
            }
        }
        EventBus.getDefault().post(chatModel)
        EventBus.getDefault().post("message_update")
    }

    fun updateGroupChatReadStatus(context: Context, mToId: String) {
        val listMessageCount =
            mAppDatabase!!.getChatDao()
                .getGroupMessageCount(
                    SharedPreferenceEditor.getData(USER_ID),
                    mToId,
                    infoMessageTypes()
                )
        val mListGroupMemberIds = ArrayList<String>()
        val mGroupDetails =
            mAppDatabase!!.getGroupMemberDao()
                .getGroupMembers(mToId, SharedPreferenceEditor.getData(USER_ID))
        if (mGroupDetails.isNotEmpty()) {
            for (i in mGroupDetails.indices) {
                mListGroupMemberIds.add(mGroupDetails[i].memberId)
            }
        }
        if (listMessageCount.isNotEmpty()) {
            val mListMessageId = ArrayList<String>()
            for (i in listMessageCount.indices) {
                mListMessageId.add(listMessageCount[i].messageId)
            }
            val mGroupChatReadModel = GroupChatReadModel()
            mGroupChatReadModel.sender = SharedPreferenceEditor.getData(USER_ID)
            mGroupChatReadModel.groupId = mToId
            mGroupChatReadModel.messageId = mListMessageId
            mGroupChatReadModel.receivers = mListGroupMemberIds

            if (ChatApp.mSocketHelper?.getSocketInstance()?.connected()!!) {
                mAppDatabase!!.getChatDao()
                    .updateGroupChatReadStatus(mToId, SharedPreferenceEditor.getData(USER_ID))
                val listChatRecent =
                    mAppDatabase!!.getChatListDao()
                        .getChatList(mToId, SharedPreferenceEditor.getData(USER_ID))
                if (listChatRecent.isNotEmpty()) {
                    for (i in listChatRecent.indices) {
                        val listUpdatedMessageCount = mAppDatabase!!.getChatDao()
                            .getGroupMessageCount(
                                SharedPreferenceEditor.getData(USER_ID),
                                mToId,
                                infoMessageTypes()
                            )
                        listChatRecent[i].messageCount = listUpdatedMessageCount.size
                        mAppDatabase!!.getChatListDao().update(listChatRecent[i])
                    }
                }
                val mGroupChatReadJson = Gson().toJson(mGroupChatReadModel)
                val mGroupChatReadObject = JSONObject(mGroupChatReadJson)
                ChatApp.mSocketHelper?.groupChatRead(mGroupChatReadObject)
                EventBus.getDefault().post("message_update")
            }
        }
    }

    /*private fun ackEvent(context: Context, event: String) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sender", SharedPreferenceEditor.getData(USER_ID))
            jsonObject.put("event ", event)
            if (mSocket.connected()) {
                mSocket.emit(ACK_EVENT, jsonObject)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/
    companion object {
        lateinit var mSocket: Socket
    }
}