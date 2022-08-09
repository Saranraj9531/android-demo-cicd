package com.sparkout.chat.ui.groupchat.view

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifOptions
import com.bumptech.glide.request.RequestOptions
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.BaseUtils.Companion.getLocalTime
import com.sparkout.chat.common.BaseUtils.Companion.getStaticMap
import com.sparkout.chat.common.BaseUtils.Companion.getTime
import com.sparkout.chat.common.BaseUtils.Companion.pickImageFromStorage
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsT
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsTv
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.databinding.ActivityGroupChatBinding
import com.sparkout.chat.databinding.ActivityGroupChatInfoBinding
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.exoplayer.view.ExoPlayerActivity
import com.sparkout.chat.ui.groupchat.view.adapter.GroupChatInfoAdapter
import com.sparkout.chat.ui.pinchzoom.view.ImageZoomActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class GroupChatInfoActivity : AppCompatActivity() {
    lateinit var mChatModel: ChatModel
    var mGroupId: String = ""
    private lateinit var binding: ActivityGroupChatInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mGroupId = intent.getStringExtra("group_id")!!
        mChatModel = intent.extras!!.getSerializable("group_chat") as ChatModel


        setSupportActionBar(binding.toolbarMessageInfo)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbarMessageInfo.navigationIcon!!.setColorFilter(
            resources.getColor(R.color.color_primary),
            PorterDuff.Mode.SRC_ATOP
        )

        setAdapters()


        binding.collapsingMessageInfo.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val availableHeight: Int = binding.collapsingMessageInfo.measuredHeight
                if (availableHeight > 0) {
                    binding.collapsingMessageInfo.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    binding.imageviewGroupInfoBackground.layoutParams.height = availableHeight
                    binding.imageviewGroupInfoBackground.visibility = View.VISIBLE
                    binding.imageviewGroupInfoBackground.background =
                        resources.getDrawable(R.drawable.default_wallpaper)
                }
            }
        })

        if (mChatModel.checkReply && mChatModel.messageType == ChatMessageTypes.TEXT.type) {
            val listChatModel =
                mAppDatabase!!.getChatDao()
                    .getGroupChatData(
                        mChatModel.replyMessageId!!, SharedPreferenceEditor.getData(
                            Global.USER_ID
                        )
                    )
            binding.layoutInfoReply.visibility = View.VISIBLE

            if (listChatModel.isNotEmpty()) {
                if (listChatModel[0].sender == SharedPreferenceEditor.getData(USER_ID)) {
                    binding.textviewReplierName.text = resources.getString(R.string.str_you)
                } else {
                    val userDetailsModel = mAppDatabase!!.getUserDetailsDao()
                        .getUserDetails(listChatModel[0].sender)
                    if (userDetailsModel.isNotEmpty()) {
                        binding.textviewReplierName.text = userDetailsModel[0].username
                    }
                }
                if (listChatModel[0].messageType == ChatMessageTypes.TEXT.type) {
                    binding.imageviewReply.visibility = View.GONE
                    binding.textviewReply.text = listChatModel[0].message
                } else if (listChatModel[0].messageType == ChatMessageTypes.AUDIO.type) {
                    binding.imageviewReply.visibility = View.GONE
                    binding.textviewReply.text =
                        BaseUtils.getLastMessage(listChatModel[0].messageType)
                } else if (listChatModel[0].messageType == ChatMessageTypes.IMAGE.type ||
                    listChatModel[0].messageType == ChatMessageTypes.VIDEO.type ||
                    listChatModel[0].messageType == ChatMessageTypes.GIF.type
                ) {
                    binding.imageviewReply.visibility = View.VISIBLE
                    when (listChatModel[0].messageType) {
                        ChatMessageTypes.VIDEO.type -> {
                            Glide.with(this)
                                .setDefaultRequestOptions(requestOptionsTv()!!)
                                .load(listChatModel[0].uri)
                                .into(binding.imageviewReply)
                        }
                        ChatMessageTypes.GIF.type -> {
                            Glide.with(this)
                                .setDefaultRequestOptions(requestOptionsT()!!)
                                .asGif()
                                .apply(
                                    RequestOptions().set(
                                        GifOptions.DISABLE_ANIMATION,
                                        false
                                    )
                                )
                                .load(listChatModel[0].uri)
                                .into(binding.imageviewReply)
                        }
                        else -> {
                            Glide.with(this)
                                .setDefaultRequestOptions(requestOptionsT()!!)
                                .load(listChatModel[0].uri)
                                .into(binding.imageviewReply)
                        }
                    }
                    binding.textviewReply.text =
                        BaseUtils.getLastMessage(listChatModel[0].messageType)
                } else if (listChatModel[0].messageType == ChatMessageTypes.LOCATION.type ||
                    listChatModel[0].messageType == ChatMessageTypes.LIVELOCATION.type
                ) {
                    binding.imageviewReply.visibility = View.VISIBLE
                    val mMapUrlReply =
                        getStaticMap(
                            this, listChatModel[0].latitude.toString()
                                .plus(",")
                                .plus(listChatModel[0].longitude.toString())
                        )
                    Glide.with(this)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .load(mMapUrlReply)
                        .into(binding.imageviewReply)
                    binding.textviewReply.text =
                        BaseUtils.getLastMessage(listChatModel[0].messageType)
                } else if (listChatModel[0].messageType == ChatMessageTypes.DOCUMENT.type) {
                    binding.imageviewReply.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(R.drawable.ic_document)
                        .into(binding.imageviewReply)
                    binding.textviewReply.text =
                        BaseUtils.getLastMessage(listChatModel[0].messageType)
                } else {
                    binding.imageviewReply.visibility = View.GONE
                    binding.textviewReply.text = resources.getString(R.string.str_reply)
                }
            } else {
                binding.imageviewReply.visibility = View.GONE
                binding.textviewReply.text = resources.getString(R.string.str_deleted)
            }

            binding.textviewReplyMsg.text =
                mChatModel.message.plus(resources.getString(R.string.string_chat_spaces))
            val mTime = getLocalTime(mChatModel.chatTime.toLong())
            binding.textviewReplyTime.visibility = View.VISIBLE
            binding.textviewReplyTime.text = getTime(mTime)
        } else {
            when (mChatModel.messageType) {
                ChatMessageTypes.TEXT.type -> {
                    binding.layoutMessageInfoText.visibility = View.VISIBLE
                    binding.textviewMessageInfo.text = mChatModel.message
                    val mTime = getLocalTime(mChatModel.chatTime.toLong())
                    binding.textviewMessageTime.text = getTime(mTime)
                }
                ChatMessageTypes.IMAGE.type -> {
                    binding.layoutMessageInfoImage.visibility = View.VISIBLE
                    val cw = ContextWrapper(applicationContext)
                    val folder = cw.getDir("Photos", Context.MODE_PRIVATE)
                    val imageDirectoryPath =
                        folder.absolutePath.plus("/").plus(mChatModel.messageId).plus(".jpg")
                    Glide.with(this)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .load(imageDirectoryPath)
                        .into(binding.imageviewInfoImage)
                    val mTime = getLocalTime(mChatModel.chatTime.toLong())
                    binding.textviewInfoImageTime.text = getTime(mTime)

                    binding.imageviewInfoImage.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            if (null != pickImageFromStorage(
                                    this@GroupChatInfoActivity,
                                    mChatModel.messageId,
                                    1
                                )
                            ) {
                                startActivity(
                                    Intent(
                                        this@GroupChatInfoActivity,
                                        ImageZoomActivity::class.java
                                    )
                                        .putExtra("value", 1)
                                        .putExtra("PATH", imageDirectoryPath)
                                )
                            } else {
                                startActivity(
                                    Intent(
                                        this@GroupChatInfoActivity,
                                        ImageZoomActivity::class.java
                                    )
                                        .putExtra("value", 2)
                                        .putExtra("URL", mChatModel.uri)
                                )
                            }
                        }
                    })
                }
                ChatMessageTypes.AUDIO.type -> {
                    binding.layoutMessageInfoAudio.visibility = View.VISIBLE
                    val mTime = getLocalTime(mChatModel.chatTime.toLong())
                    binding.textviewMessageInfoAudioTime.text = getTime(mTime)

                }
                ChatMessageTypes.VIDEO.type -> {
                    binding.layoutInfoVideo.visibility = View.VISIBLE
                    val mTime = getLocalTime(mChatModel.chatTime.toLong())
                    binding.textviewInfoVideoTime.text = getTime(mTime)
                    val cw = ContextWrapper(applicationContext)
                    val folder = cw.getDir("Videos", Context.MODE_PRIVATE)
                    val mVideoDirectoryPath =
                        folder.absolutePath.plus("/").plus(mChatModel.messageId).plus(".mp4")
                    Glide.with(this)
                        .setDefaultRequestOptions(requestOptionsTv()!!)
                        .load(mVideoDirectoryPath)
                        .into(binding.imageviewInfoVideo)

                    binding.cardInfoVideo.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            if (null != pickImageFromStorage(
                                    this@GroupChatInfoActivity,
                                    mChatModel.messageId,
                                    2
                                )
                            ) {
                                startActivity(
                                    Intent(
                                        this@GroupChatInfoActivity,
                                        ExoPlayerActivity::class.java
                                    )
                                        .putExtra("URL", mVideoDirectoryPath)
                                )
                            } else {
                                startActivity(
                                    Intent(
                                        this@GroupChatInfoActivity,
                                        ExoPlayerActivity::class.java
                                    )
                                        .putExtra("URL", mChatModel.uri)
                                )
                            }
                        }
                    })
                }
                ChatMessageTypes.DOCUMENT.type -> {
                    binding.layoutInfoDocument.visibility = View.VISIBLE
                    val mTime = getLocalTime(mChatModel.chatTime.toLong())
                    binding.textviewInfoDocumentTime.text = getTime(mTime)
                    binding.textviewDocumentName.text = mChatModel.message

                    binding.cardInfoDocument.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            var mFilePath: String? = null
                            mFilePath =
                                if (null != pickImageFromStorage(
                                        this@GroupChatInfoActivity,
                                        mChatModel.messageId,
                                        4
                                    )
                                ) {
                                    pickImageFromStorage(
                                        this@GroupChatInfoActivity,
                                        mChatModel.messageId,
                                        4
                                    )
                                } else {
                                    mChatModel.uri
                                }
                            val target = Intent(Intent.ACTION_VIEW)
                            target.setDataAndType(
                                Uri.fromFile(File(mFilePath!!)),
                                "application/pdf"
                            )
                            target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                            val intent = Intent.createChooser(target, "Open File")
                            try {
                                startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                // Instruct the user to install a PDF reader here, or something
                            }
                        }
                    })
                }
                ChatMessageTypes.GIF.type -> {
                    binding.layoutMessageInfoImage.visibility = View.VISIBLE
                    val mTime = getLocalTime(mChatModel.chatTime.toLong())
                    binding.textviewInfoImageTime.text = getTime(mTime)
                    Glide.with(this)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .asGif()
                        .apply(RequestOptions().set(GifOptions.DISABLE_ANIMATION, false))
                        .load(mChatModel.uri)
                        .into(binding.imageviewInfoImage)
                }
                ChatMessageTypes.LOCATION.type -> {
                    val mMapUrl: String =
                        getStaticMap(
                            this, mChatModel.latitude.toString()
                                .plus(",")
                                .plus(mChatModel.longitude.toString())
                        )!!
                    binding.layoutMessageInfoLocation.visibility = View.VISIBLE
                    val mTime = getLocalTime(mChatModel.chatTime.toLong())
                    binding.textviewInfoLocationTime.text = getTime(mTime)
                    Glide.with(this)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .load(mMapUrl)
                        .into(binding.imageviewInfoLocation)
                }
            }
        }
    }

    private fun setAdapters() {
        var mListSeen = ArrayList<String>()
        val mListDelivered = ArrayList<String>()
        val mMessageStatusModel =
            mAppDatabase!!.getMessageStatusDao()
                .getMessageList(mChatModel.messageId, SharedPreferenceEditor.getData(USER_ID))
        if (mMessageStatusModel.isNotEmpty()) {
            mListSeen = mMessageStatusModel[0].seenId

            for (i in mMessageStatusModel[0].deliveredId.indices) {
                if (!mListSeen.contains(mMessageStatusModel[0].deliveredId[i])) {
                    mListDelivered.add(mMessageStatusModel[0].deliveredId[i])
                }
            }



            if (mListDelivered.isNotEmpty()) {
                binding.cardMessageDelivered.visibility = View.VISIBLE
                binding.rvMessageDelivered.adapter = GroupChatInfoAdapter(this, mListDelivered)
            } else {
                binding.cardMessageDelivered.visibility = View.GONE
            }


            if (mListSeen.isNotEmpty()) {
                binding.cardMessageSeen.visibility = View.VISIBLE
                binding.rvMessageSeen.adapter = GroupChatInfoAdapter(this, mListSeen)
            } else {
                binding.cardMessageSeen.visibility = View.GONE
            }
        }
        val mListIds = ArrayList<String>()
        val mListNotDelivered = ArrayList<String>()
        val mGroupDetails = mAppDatabase!!.getGroupMemberDao().getGroupMembers(
            mGroupId,
            SharedPreferenceEditor.getData(
                USER_ID
            )
        )
        if (mGroupDetails.isNotEmpty()) {
            for (i in mGroupDetails.indices) {
                mListIds.add(mGroupDetails[i].memberId)
            }
            if (mListDelivered.isNotEmpty()) {
                for (i in mListIds.indices) {
                    if (!mListDelivered.contains(mListIds[i])) {
                        mListNotDelivered.add(mListIds[i])
                    }
                }
                if (mListNotDelivered.isNotEmpty()) {
                    binding.textviewDeliveredRemaining.text = mListNotDelivered.size.toString()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(mString: String) {
        if (mString == "update_deliver_read") {
            setAdapters()
        }
    }
}