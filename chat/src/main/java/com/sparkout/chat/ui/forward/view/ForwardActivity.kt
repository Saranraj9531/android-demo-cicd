package com.sparkout.chat.ui.forward.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.BaseUtils.Companion.getMessageId
import com.sparkout.chat.common.BaseUtils.Companion.getUTCTime
import com.sparkout.chat.common.BaseUtils.Companion.loadAudioPath
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMessageStatus
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.common.model.LocationModel
import com.sparkout.chat.databinding.ActivityForwardBinding
import com.sparkout.chat.ui.chat.model.ChatListModel
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.chat.view.ChatActivity
import com.sparkout.chat.ui.forward.view.adapter.ForwardAdapter
import com.sparkout.chat.ui.groupchat.view.GroupChatActivity
import org.json.JSONObject
import java.io.*
import java.util.*
import kotlin.jvm.Throws

class ForwardActivity : AppCompatActivity() {
    private var chatPojoList: List<ChatListModel>? = null
    lateinit var chatModelForward: ChatModel
    lateinit var mForwardAdapter: ForwardAdapter
    private val RECORDER_BPP: Int = 16
    private var RECORDER_SAMPLERATE: Int = 8000
    val bufferSize: Int = 1024
    var in1: FileInputStream? = null
    var out: FileOutputStream? = null
    var totalAudioLen: Long = 0
    var totalDataLen = totalAudioLen + 36
    val longSampleRate = RECORDER_SAMPLERATE.toLong()
    val channels = 1
    val byteRate: Long = (RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8).toLong()
    val data = ByteArray(bufferSize)
    private lateinit var binding: ActivityForwardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForwardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back_arrow)

        chatModelForward = intent.extras!!.getSerializable("messageForward") as ChatModel


        binding.rvMessageForward.layoutManager = LinearLayoutManager(this)

        chatPojoList = mAppDatabase!!.getChatListDao().getChatList(
            SharedPreferenceEditor.getData(
                Global.USER_ID
            )
        )
        mForwardAdapter = ForwardAdapter(this, chatPojoList!!)
        binding.rvMessageForward.adapter = mForwardAdapter

        binding.edittextSearchForward.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (chatPojoList!!.isNotEmpty()) {
                    mForwardAdapter.getFilter()?.filter(s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * fun to get the selected contact from forward list and send to evet
     **/
    @SuppressLint("SimpleDateFormat")
    fun selectedContact(chatListModel: ChatListModel) {
        val mReceiverId = chatListModel.receiverId
        if (chatListModel.chatType == ChatTypes.GROUP.type) {
            val mGroupMembersId = ArrayList<String>()
            val mListGroupMembers =
                mAppDatabase!!.getGroupMemberDao().getGroupMembers(
                    mReceiverId, SharedPreferenceEditor.getData(
                        Global.USER_ID
                    )
                )
            if (mListGroupMembers.isNotEmpty()) {
                for (i in 0 until mListGroupMembers.size) {
                    mGroupMembersId.add(mListGroupMembers[i].memberId)
                }
            }
            val mMessageId =
                getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(Global.USER_ID))
                    .plus("-").plus(mReceiverId)
            val chatModel = ChatModel()
            chatModel.checkForwarded = true
            chatModel.checkReply = false
            chatModel.message = chatModelForward.message
            chatModel.messageId = getMessageId(mMessageId)
            chatModel.messageStatus = ChatMessageStatus.NOT_SENT.ordinal
            chatModel.messageType = chatModelForward.messageType
            chatModel.receiver = mReceiverId
            chatModel.receivers = mGroupMembersId
            chatModel.sender = SharedPreferenceEditor.getData(Global.USER_ID)
            chatModel.chatTime = getUTCTime()
            chatModel.chatType = ChatTypes.GROUP.type
            if (null != chatModelForward.latitude) {
                chatModel.latitude = chatModelForward.latitude
            }
            if (null != chatModelForward.longitude) {
                chatModel.longitude = chatModelForward.longitude
            }
            if (null != chatModelForward.uri) {
                chatModel.uri = chatModelForward.uri
            }
            if (null != chatModelForward.duration) {
                chatModel.duration = chatModelForward.duration
            }

            if (chatModelForward.messageType == ChatMessageTypes.LOCATION.type.toString()) {
                val listUserDetails = mAppDatabase!!.getLocationDao()
                    .getLocationMessage(
                        chatModel.messageId,
                        SharedPreferenceEditor.getData(Global.USER_ID)
                    )
                val mLocationModel = LocationModel()
                mLocationModel.messageId = chatModel.messageId
                mLocationModel.latitude = chatModelForward.latitude!!
                mLocationModel.longitude = chatModelForward.longitude!!
                if (listUserDetails.isNotEmpty()) {
                    mAppDatabase!!.getLocationDao().update(mLocationModel)
                } else {
                    mAppDatabase!!.getLocationDao().insert(mLocationModel)
                }
            }
            /* try {
                 if (chatModelForward.messageType == ChatMessageTypes.AUDIO.type.toString()) {
                     var path = loadAudioPath(this, chatModelForward.messageId, 1)
                     val cw = ContextWrapper(applicationContext)
                     val folder = cw.getDir("Voice", Context.MODE_PRIVATE)
                     try {
                         val file = File(folder, getMessageId(mMessageId) + ".wav")
                         copy(path!!, file)
                     } catch (e: Exception) {
                     }
                 }
             } catch (e: Exception) {
             }*/

            if (chatModelForward.messageType == ChatMessageTypes.AUDIO.type.toString()) {
                if (chatModelForward.sender == SharedPreferenceEditor.getData(Global.USER_ID)) {
                    var path = loadAudioPath(this, chatModelForward.messageId, 1)
                    val cw = ContextWrapper(applicationContext)
                    val folder = cw.getDir("Voice", Context.MODE_PRIVATE)
                    val file = File(folder, getMessageId(mMessageId) + ".wav")
                    copy(path!!, file)
                } else {
                    var path: String = ""
                    if (null != loadAudioPath(this, chatModelForward.messageId, 2)) {
                        path =
                            loadAudioPath(this, chatModelForward.messageId, 2)!!
                        val cw = ContextWrapper(applicationContext)
                        val folder = cw.getDir("Voice", Context.MODE_PRIVATE)
                        val file = File(folder, getMessageId(mMessageId) + ".wav")
                        copy(path, file)
                    }
                }
            }
            val chatJson = Gson().toJson(chatModel)
            val jsonObject = JSONObject(chatJson)
            ChatApp.mSocketHelper?.sendGroupChat(jsonObject)

            startActivity(
                Intent(this, GroupChatActivity::class.java)
                    .putExtra("id", mReceiverId)
            )
            finish()
        } else {
            val mMessageId = getUTCTime()
                .plus("-").plus(SharedPreferenceEditor.getData(Global.USER_ID)).plus("-")
                .plus(mReceiverId)
            val chatModel = ChatModel()
            chatModel.checkForwarded = true
            chatModel.checkReply = false
            chatModel.message = chatModelForward.message
            chatModel.messageId = getMessageId(mMessageId)
            chatModel.messageStatus = ChatMessageStatus.NOT_SENT.ordinal
            chatModel.messageType = chatModelForward.messageType
            chatModel.receiver = mReceiverId
            chatModel.sender = SharedPreferenceEditor.getData(Global.USER_ID)
            chatModel.chatTime = getUTCTime()
            chatModel.chatType = ChatTypes.SINGLE.type
            if (null != chatModelForward.latitude) {
                chatModel.latitude = chatModelForward.latitude
            }
            if (null != chatModelForward.longitude) {
                chatModel.longitude = chatModelForward.longitude
            }
            if (null != chatModelForward.uri) {
                chatModel.uri = chatModelForward.uri
            }
            if (null != chatModelForward.duration) {
                chatModel.duration = chatModelForward.duration
            }
            if (chatModelForward.messageType == ChatMessageTypes.LOCATION.type.toString()) {
                val listUserDetails = mAppDatabase!!.getLocationDao()
                    .getLocationMessage(
                        chatModel.messageId,
                        SharedPreferenceEditor.getData(Global.USER_ID)
                    )
                val mLocationModel = LocationModel()
                mLocationModel.messageId = chatModel.messageId
                mLocationModel.latitude = chatModelForward.latitude!!
                mLocationModel.longitude = chatModelForward.longitude!!
                if (listUserDetails.isNotEmpty()) {
                    mAppDatabase!!.getLocationDao().update(mLocationModel)
                } else {
                    mAppDatabase!!.getLocationDao().insert(mLocationModel)
                }
            }

            if (chatModelForward.messageType == ChatMessageTypes.AUDIO.type.toString()) {
                if (chatModelForward.sender == SharedPreferenceEditor.getData(Global.USER_ID)) {
                    var path = loadAudioPath(this, chatModelForward.messageId, 1)
                    val cw = ContextWrapper(applicationContext)
                    val folder = cw.getDir("Voice", Context.MODE_PRIVATE)
                    val file = File(folder, getMessageId(mMessageId) + ".wav")
                    copy(path!!, file)
                } else {
                    var path: String = ""
                    if (null != loadAudioPath(this, chatModelForward.messageId, 2)) {
                        path =
                            loadAudioPath(this, chatModelForward.messageId, 2)!!
                        val cw = ContextWrapper(applicationContext)
                        val folder = cw.getDir("Voice", Context.MODE_PRIVATE)
                        val file = File(folder, getMessageId(mMessageId) + ".wav")
                        copy(path, file)
                    }
                }
            }

            mAppDatabase!!.getChatDao().insert(chatModel)
            val chatJson = Gson().toJson(chatModel)
            val jsonObject = JSONObject(chatJson)
            /* val checkFriendExists =
                 mAppDatabase!!.getFriendDetailsDao()
                     .checkFriendAlreadyExists(mReceiverId, getUserId(this)!!)
             if (checkFriendExists.isEmpty()) {
                 val friendDetailsModel = FriendDetailsModel()
                 friendDetailsModel.sender = getUserId(this)!!
                 friendDetailsModel.receiver = mReceiverId
                 val friendDetailsJson = Gson().toJson(friendDetailsModel)
                 val jsonObjectFriend = JSONObject(friendDetailsJson)
                 if (isOnline(this)) {
                     ChatApp.mSocketHelper?.sendAddContact(jsonObjectFriend)
                 }
             }*/
            ChatApp.mSocketHelper?.sendSingleChat(jsonObject)

            startActivity(
                Intent(this, ChatActivity::class.java)
                    .putExtra("id", mReceiverId)
            )
            finish()
        }
    }

    @Throws(IOException::class)
    fun copy(src: String, dst: File?) {
        val input: InputStream = FileInputStream(src)
        try {
            val out: OutputStream = FileOutputStream(dst)
            try {
                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len: Int
                while (input.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            } finally {
                out.close()
            }
        } finally {
            input.close()
        }
    }
}
