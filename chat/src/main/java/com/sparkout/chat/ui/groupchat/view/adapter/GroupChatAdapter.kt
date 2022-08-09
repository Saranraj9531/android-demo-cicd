package com.sparkout.chat.ui.groupchat.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifOptions
import com.bumptech.glide.request.RequestOptions
import com.sparkout.chat.R
import com.sparkout.chat.clickinterface.GroupChatClickOptions
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.BaseUtils.Companion.getLastMessage
import com.sparkout.chat.common.BaseUtils.Companion.getLocalTime
import com.sparkout.chat.common.BaseUtils.Companion.getSmsTodayYestFromMilli
import com.sparkout.chat.common.BaseUtils.Companion.getStaticMap
import com.sparkout.chat.common.BaseUtils.Companion.getTime
import com.sparkout.chat.common.BaseUtils.Companion.pickImageFromStorage
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsD
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsT
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsTv
import com.sparkout.chat.common.BaseUtils.Companion.validDownload
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMessageStatus
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.chat.view.MessageDiffCallback
import com.sparkout.chat.ui.groupchat.view.GroupChatActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

// Created by krish on 20-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class GroupChatAdapter(
    val context: Context,
    private val listChat: ArrayList<ChatModel>,
    val chatClickOptions: GroupChatClickOptions,
    private val unreadMessageCount: Int,
    val groupChatActivity: GroupChatActivity
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //    private val PAYMENT = 6
    private val EMPTY = 0
    private val REPLY = 32
    private val CHAT_DESC = 20
    private val MISSED_CALL = 33
    private val CHAT_DATE = 101
    public var selectedPosition: Int = -1
    private var dis: Int = 0

    /*fun updateMessageListItems(messageList: ArrayList<ChatModel>, rv_chat: RecyclerView?) {
        val diffCallback = MessageDiffCallback(this.listChat, messageList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.listChat.clear()
        this.listChat.addAll(messageList)
        diffResult.dispatchUpdatesTo(this)
        rv_chat?.smoothScrollToPosition(0)
    }*/
    fun updateMessageListItems(
        messageList: ArrayList<ChatModel>,
        rv_chat: RecyclerView?,
        locChatModel: ChatModel?,
        needUpdate: Boolean
    ) {
        if (selectedPosition != -1) {
            if (messageList.size > listChat.size) {
                selectedPosition += 1
            } else if (messageList.size < listChat.size) {
                selectedPosition -= 1
            }
        }
        val diffCallback =
            MessageDiffCallback(this.listChat, messageList, locChatModel, needUpdate)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.listChat.clear()
        this.listChat.addAll(messageList)
        diffResult.dispatchUpdatesTo(this)
        val SCROLLING_UP = -1

        rv_chat?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                dis = dy
            }
        })

        if (rv_chat != null) {
            val scrollToNewTop: Boolean = !rv_chat.canScrollVertically(SCROLLING_UP)
            if (scrollToNewTop && dis == 0) {
                rv_chat.scrollToPosition(0)
            } else {
                rv_chat.scrollToPosition(0)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            EMPTY -> {
                view = inflater.inflate(R.layout.item_chat_message, parent, false)
                return ChatViewHolder(view)
            }
            ChatMessageTypes.TEXT.type.toInt() -> {
                view = inflater.inflate(R.layout.item_chat_message, parent, false)
                return ChatViewHolder(view)
            }
            ChatMessageTypes.AUDIO.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_sample,
                    parent,
                    false
                )
                return AudioViewHolder(view)
            }
            ChatMessageTypes.VIDEO.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_video,
                    parent,
                    false
                )
                return VideoViewHolder(view)
            }
            ChatMessageTypes.IMAGE.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_image,
                    parent,
                    false
                )
                return ImageViewHolder(view)
            }
            ChatMessageTypes.DOCUMENT.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_document,
                    parent,
                    false
                )
                return DocumentViewHolder(view)
            }
            ChatMessageTypes.GIF.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_image,
                    parent,
                    false
                )
                return ImageViewHolder(view)
            }
            ChatMessageTypes.LOCATION.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_location,
                    parent,
                    false
                )
                return LocationViewHolder(view)
            }
            ChatMessageTypes.DATE.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_chat_date,
                    parent,
                    false
                )
                return ChatDateViewHolder(view)
            }
            ChatMessageTypes.UNREAD.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_chat_unread,
                    parent,
                    false
                )
                return ChatUnreadViewHolder(view)
            }
            REPLY -> {
                view = inflater.inflate(
                    R.layout.item_reply,
                    parent,
                    false
                )
                return ReplyViewHolder(view)
            }
            ChatMessageTypes.CREATEGROUP.type.toInt(),
            ChatMessageTypes.GROUPINFO.type.toInt(),
            ChatMessageTypes.ADDMEMBER.type.toInt(),
            ChatMessageTypes.REMOVEMEMBER.type.toInt(),
            ChatMessageTypes.EXITMEMBER.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_group_descriptions,
                    parent,
                    false
                )
                return GroupDescriptionViewHolder(view)
            }
            ChatMessageTypes.GROUPPROFILE.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_group_icon_change,
                    parent,
                    false
                )
                return GroupProfileViewHolder(view)
            }
            ChatMessageTypes.DELETEFOREVERYONE.type.toInt() -> {
                view = inflater.inflate(
                    R.layout.item_deleted_message,
                    parent,
                    false
                )
                return DeletedMessageViewHolder(view)
            }
        }
        return null!!
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val listGroupMemberDetail =
            mAppDatabase!!.getUserDetailsDao().getUserDetails(listChat[position].sender)
        when (getItemViewType(position)) {
            EMPTY -> {
                val emptyHolder = holder as ChatViewHolder
                /*if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                    emptyHolder.layoutInMessage.visibility = View.GONE
                    emptyHolder.layoutOutMessage.visibility = View.VISIBLE
                    emptyHolder.textViewOutMessage.text = listChat[position].message
                } else {
                    emptyHolder.layoutOutMessage.visibility = View.GONE
                    emptyHolder.layoutInMessage.visibility = View.VISIBLE
                    emptyHolder.textViewInMessage.text = listChat[position].message
                }*/
            }
            ChatMessageTypes.TEXT.type.toInt() -> {
                val chatHolder = holder as ChatViewHolder
                if (listChat[position].sender == SharedPreferenceEditor.getData(Global.USER_ID)) {
                    chatHolder.layoutInMessage.visibility = View.GONE
                    chatHolder.layoutOutMessage.visibility = View.VISIBLE
                    if (listChat[position].checkForwarded) {
                        chatHolder.textViewForwardedOut.visibility = View.VISIBLE
                    } else {
                        chatHolder.textViewForwardedOut.visibility = View.GONE
                    }
                    if (listChat[position].checkEdit) {
                        chatHolder.imageViewEdited.visibility = View.VISIBLE
                    } else {
                        chatHolder.imageViewEdited.visibility = View.GONE
                    }
                    chatHolder.textViewOutMessage.text =
                        listChat[position].message.plus(context.resources.getString(R.string.string_chat_spaces))
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    chatHolder.textViewOutMessageTime.text = getTime(mTime)
                    when (listChat[position].messageStatus) {
                        0 -> {
                            chatHolder.textViewOutMessageTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_not_deliver,
                                0
                            )
                        }
                        1 -> {
                            chatHolder.textViewOutMessageTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_send,
                                0
                            )
                        }
                        2 -> {
                            chatHolder.textViewOutMessageTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_delivered,
                                0
                            )
                        }
                        3 -> {
                            chatHolder.textViewOutMessageTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_read,
                                0
                            )
                        }
                    }
                } else {
                    chatHolder.layoutOutMessage.visibility = View.GONE
                    chatHolder.layoutInMessage.visibility = View.VISIBLE
                    if (listGroupMemberDetail.isNotEmpty()) {
                        chatHolder.textViewSenderName.visibility = View.VISIBLE
                        chatHolder.textViewSenderName.text = listGroupMemberDetail[0].username
                    } else {
                        chatHolder.textViewSenderName.visibility = View.VISIBLE
                        chatHolder.textViewSenderName.text = "Not your Friend"
                    }
                    if (listChat[position].checkForwarded) {
                        chatHolder.textViewForwardedIn.visibility = View.VISIBLE
                    } else {
                        chatHolder.textViewForwardedIn.visibility = View.GONE
                    }
                    if (listChat[position].checkEdit) {
                        chatHolder.imageViewEditedIn.visibility = View.VISIBLE
                    } else {
                        chatHolder.imageViewEditedIn.visibility = View.GONE
                    }
                    chatHolder.textViewInMessage.text =
                        listChat[position].message.plus(context.resources.getString(R.string.string_chat_spaces_receiver))
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    chatHolder.textViewInMessageTime.text = getTime(mTime)
                }
            }
            ChatMessageTypes.AUDIO.type.toInt() -> {
                val audioHolder = holder as GroupChatAdapter.AudioViewHolder
                var mSeekBar: SeekBar
                var textViewRuntimeAudio: AppCompatTextView
                var textViewTotalTimeAudio: AppCompatTextView
                var imageViewPlaySendAudio: AppCompatImageView
                var textViewAudioTime: AppCompatTextView


                holder.imageViewMicSentAudio.setImageResource(R.drawable.ic_mic)
                holder.imageViewMicReceivedAudio.setImageResource(R.drawable.ic_mic)
                if ("null" == listChat[audioHolder.adapterPosition].uri) {
                    if (listChat[audioHolder.adapterPosition].sender == SharedPreferenceEditor.getData(
                            Global.USER_ID
                        ) && listChat[audioHolder.adapterPosition].messageStatus == ChatMessageStatus.RETRY.ordinal
                    ) {
                        audioHolder.layoutSentAudioRetry.visibility = View.VISIBLE
                    } else {
                        audioHolder.layoutSentAudioRetry.visibility = View.GONE
                    }
                    if (listChat[audioHolder.adapterPosition].sender == SharedPreferenceEditor.getData(
                            Global.USER_ID
                        )
                    ) {
                        audioHolder.textViewAudioSentTime.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_message_not_deliver,
                            0
                        )
                    }
                } else {
                    if (listChat[audioHolder.adapterPosition].sender == SharedPreferenceEditor.getData(
                            Global.USER_ID
                        )
                    ) {
                        when (listChat[audioHolder.adapterPosition].messageStatus) {
                            0 -> {
                                audioHolder.textViewAudioSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_not_deliver,
                                    0
                                )
                            }
                            1 -> {
                                audioHolder.textViewAudioSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_send,
                                    0
                                )
                            }
                            2 -> {
                                audioHolder.textViewAudioSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_delivered,
                                    0
                                )
                            }
                            3 -> {
                                audioHolder.textViewAudioSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_read,
                                    0
                                )
                            }
                        }
                    }
                }
                if (listChat[audioHolder.adapterPosition].sender == SharedPreferenceEditor.getData(
                        Global.USER_ID
                    )
                ) {
                    mSeekBar = audioHolder.seekarBarSentAudio
                    textViewRuntimeAudio = audioHolder.textViewSentAudioRunTime
                    textViewTotalTimeAudio = audioHolder.textViewSentAudioTotalTime
                    imageViewPlaySendAudio = audioHolder.imageViewPlaySentAudio
                    textViewAudioTime = audioHolder.textViewAudioSentTime
                    audioHolder.layoutAudioReceived.visibility = View.GONE
                    audioHolder.layoutAudioSent.visibility = View.VISIBLE
                    if (listChat[audioHolder.adapterPosition].checkForwarded) {
                        audioHolder.textViewForwardedOut.visibility = View.VISIBLE
                        Log.e(
                            "Nive ",
                            "onBindViewHolder:Group ${listChat[audioHolder.adapterPosition].uri}"
                        )
                    } else {
                        audioHolder.textViewForwardedOut.visibility = View.GONE
                    }
                } else {
                    audioHolder.layoutAudioSent.visibility = View.GONE
                    audioHolder.layoutAudioReceived.visibility = View.VISIBLE
                    mSeekBar = audioHolder.seekarBarReceivedAudio
                    textViewRuntimeAudio = audioHolder.textViewReceivedAudioRunTime
                    textViewTotalTimeAudio = audioHolder.textViewReceivedAudioTotalTime
                    imageViewPlaySendAudio = audioHolder.imageViewPlayReceivedAudio
                    textViewAudioTime = audioHolder.textViewAudioReceivedTime
                    if (listChat[audioHolder.adapterPosition].checkForwarded) {
                        audioHolder.textViewForwardedIn.visibility = View.VISIBLE
                    } else {
                        audioHolder.textViewForwardedIn.visibility = View.GONE
                    }
                    if (listGroupMemberDetail.isNotEmpty()) {
                        audioHolder.textViewSenderName.visibility = View.VISIBLE
                        audioHolder.textViewSenderName.text = listGroupMemberDetail[0].username
                    } else {
                        audioHolder.textViewSenderName.visibility = View.VISIBLE
                        audioHolder.textViewSenderName.text = "Not your Friend"
                    }
                }
                if (listChat[audioHolder.adapterPosition].isPlaying) {
                    imageViewPlaySendAudio.setImageDrawable(
                        context.resources.getDrawable(
                            R.drawable.ic_audio_pause
                        )
                    )
                } else {
                    imageViewPlaySendAudio.setImageDrawable(
                        context.resources.getDrawable(
                            R.drawable.ic_audio_play
                        )
                    )
                    mSeekBar.progress = 0
                    textViewRuntimeAudio.visibility = View.GONE
                    textViewTotalTimeAudio.visibility = View.GONE
                }
                val mTime =
                    getLocalTime(listChat[audioHolder.adapterPosition].chatTime.toLong())
                textViewAudioTime.text = getTime(mTime)
                val listAudioStatus = mAppDatabase!!.getAudioStatusDao()
                    .checkAlreadyExists(
                        listChat[audioHolder.adapterPosition].messageId,
                        SharedPreferenceEditor.getData(Global.USER_ID)
                    )
                if (listAudioStatus.isNotEmpty()) {
                    if (listAudioStatus[0].checkPlayed) {
                        audioHolder.imageViewMicSentAudio.setImageResource(R.drawable.ic_mic_played)
                        audioHolder.imageViewMicReceivedAudio.setImageResource(R.drawable.ic_mic_played)
                        if (listChat[audioHolder.adapterPosition].isPlaying) {
                            audioHolder.imageViewPlaySentAudio.setImageResource(R.drawable.ic_audio_pause)
                            audioHolder.imageViewPlayReceivedAudio.setImageResource(R.drawable.ic_audio_pause)
                        } else {
                            audioHolder.imageViewPlaySentAudio.setImageResource(R.drawable.ic_audio_played)
                            audioHolder.imageViewPlayReceivedAudio.setImageResource(R.drawable.ic_audio_played)
                        }
                    } else {
                        audioHolder.imageViewMicSentAudio.setImageResource(R.drawable.ic_mic)
                        audioHolder.imageViewMicReceivedAudio.setImageResource(R.drawable.ic_mic)
                        audioHolder.imageViewPlaySentAudio.setImageResource(R.drawable.ic_audio_play)
                        audioHolder.imageViewPlayReceivedAudio.setImageResource(R.drawable.ic_audio_play)
                    }
                }

                if (listChat[audioHolder.adapterPosition].sender == SharedPreferenceEditor.getData(
                        Global.USER_ID
                    )
                ) {
                    audioHolder.imageViewPlaySentAudio.setOnClickListener(object :
                        View.OnClickListener {
                        override fun onClick(v: View?) {
                            BaseUtils.preventDoubleClick(v!!)
                            var isSame: Boolean = false
                            if (selectedPosition != -1) {
                                if (selectedPosition == audioHolder.adapterPosition) {
                                    Log.e("Nive ", "onClick: $selectedPosition")
                                    Log.e("Nive ", "onClick:Select ${audioHolder.adapterPosition}")
                                    isSame = true
                                    if (listChat[selectedPosition].isPlaying) {
                                        listChat[selectedPosition].isPlaying = false
                                        listChat[selectedPosition].isPaused = true
                                    } else {
                                        listChat[selectedPosition].isPlaying = true
                                        listChat[selectedPosition].isPaused = false
                                    }
                                    //it resets the seekbar on pause so this is commented
                                    /* notifyItemChanged(selectedPosition)*/
                                } else {
                                    listChat[selectedPosition].isPlaying = false
                                    listChat[selectedPosition].isPaused = false
                                    notifyItemChanged(selectedPosition)
                                    selectedPosition = audioHolder.adapterPosition
                                    listChat[audioHolder.adapterPosition].isPlaying = true
                                    listChat[audioHolder.adapterPosition].isPaused = false
                                    /*notifyItemChanged(audioHolder.adapterPosition)*/
                                }
                            } else {
                                selectedPosition = audioHolder.adapterPosition
                                listChat[audioHolder.adapterPosition].isPlaying = true
                                listChat[audioHolder.adapterPosition].isPaused = false
                                /* notifyItemChanged(audioHolder.adapterPosition)*/
                            }
                            groupChatActivity.updateSeekBar(
                                audioHolder,
                                audioHolder.adapterPosition,
                                listChat[audioHolder.adapterPosition],
                                listChat[audioHolder.adapterPosition].sender,
                                listChat[audioHolder.adapterPosition].messageId,
                                isSame
                            )
                        }
                    })
                } else {
                    audioHolder.imageViewPlayReceivedAudio.setOnClickListener(object :
                        View.OnClickListener {
                        override fun onClick(v: View?) {
                            var isSame: Boolean = false
                            if (selectedPosition != -1) {
                                if (selectedPosition == audioHolder.adapterPosition) {
                                    isSame = true
                                    if (listChat[selectedPosition].isPlaying) {
                                        listChat[selectedPosition].isPlaying = false
                                        listChat[selectedPosition].isPaused = true
                                    } else {
                                        listChat[selectedPosition].isPlaying = true
                                        listChat[selectedPosition].isPaused = false
                                    }
                                    //it resets the seekbar on pause so this is commented
                                    /* notifyItemChanged(selectedPosition)*/
                                } else {
                                    listChat[selectedPosition].isPlaying = false
                                    listChat[selectedPosition].isPaused = false
                                    notifyItemChanged(selectedPosition)
                                    selectedPosition = audioHolder.adapterPosition
                                    listChat[audioHolder.adapterPosition].isPlaying = true
                                    listChat[audioHolder.adapterPosition].isPaused = false
                                    /*notifyItemChanged(audioHolder.adapterPosition)*/
                                }
                            } else {
                                selectedPosition = audioHolder.adapterPosition
                                listChat[audioHolder.adapterPosition].isPlaying = true
                                listChat[audioHolder.adapterPosition].isPaused = false
                                /* notifyItemChanged(audioHolder.adapterPosition)*/
                            }
                            groupChatActivity.updateSeekBar(
                                audioHolder,
                                audioHolder.adapterPosition,
                                listChat[audioHolder.adapterPosition],
                                listChat[audioHolder.adapterPosition].sender,
                                listChat[audioHolder.adapterPosition].messageId,
                                isSame
                            )
                        }
                    })
                }
            }
            //-----------------------------------------------------------------------------------
            ChatMessageTypes.VIDEO.type.toInt() -> {
                val videoHolder = holder as VideoViewHolder
                if ("null" == listChat[position].uri) {
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID) && listChat[position].messageStatus == ChatMessageStatus.RETRY.ordinal) {
                        videoHolder.layoutCenterViewVideo.visibility = View.GONE
                        videoHolder.layoutSenderRetry.visibility = View.VISIBLE
                    } else {
                        videoHolder.layoutSenderRetry.visibility = View.GONE
                        videoHolder.layoutCenterViewVideo.visibility = View.VISIBLE
                    }
                    videoHolder.progressBarReceivedVideo.visibility = View.VISIBLE
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                        videoHolder.textViewSentVideoTime.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_message_not_deliver,
                            0
                        )
                    }
                } else {
                    videoHolder.layoutSenderRetry.visibility = View.GONE
                    videoHolder.progressBarSentVideo.visibility = View.GONE
                    videoHolder.progressBarReceivedVideo.visibility = View.GONE
                    videoHolder.layoutCenterViewVideo.visibility = View.VISIBLE
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                        when (listChat[position].messageStatus) {
                            ChatMessageStatus.NOT_SENT.ordinal -> {
                                videoHolder.textViewSentVideoTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_not_deliver,
                                    0
                                )
                            }
                            ChatMessageStatus.SENT.ordinal -> {
                                videoHolder.textViewSentVideoTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_send,
                                    0
                                )
                            }
                            ChatMessageStatus.DELIVERED.ordinal -> {
                                videoHolder.textViewSentVideoTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_delivered,
                                    0
                                )
                            }
                            ChatMessageStatus.READ.ordinal -> {
                                videoHolder.textViewSentVideoTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_read,
                                    0
                                )
                            }
                        }
                    }
                }
                if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                    videoHolder.layoutReceivedVideo.visibility = View.GONE
                    videoHolder.layoutSentVideo.visibility = View.VISIBLE
                    if (listChat[position].checkForwarded) {
                        videoHolder.textViewForwardedOut.visibility = View.VISIBLE
                    } else {
                        videoHolder.textViewForwardedOut.visibility = View.GONE
                    }

                    if (null != pickImageFromStorage(
                            context,
                            listChat[position].messageId,
                            2
                        )
                    ) {
                        Glide.with(context)
                            .setDefaultRequestOptions(requestOptionsTv()!!)
                            .load(
                                pickImageFromStorage(
                                    context,
                                    listChat[position].messageId,
                                    2
                                )
                            )
                            .into(videoHolder.imageViewSentVideo)
                    } else {
                        Glide.with(context)
                            .setDefaultRequestOptions(requestOptionsTv()!!)
                            .load(listChat[position].uri)
                            .into(videoHolder.imageViewSentVideo)
                    }
                    videoHolder.layoutSentVideo.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            chatClickOptions.videoOptions(listChat[videoHolder.adapterPosition])
                        }
                    })
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    videoHolder.textViewSentVideoTime.text = getTime(mTime)
                } else {
                    videoHolder.layoutSentVideo.visibility = View.GONE
                    videoHolder.layoutReceivedVideo.visibility = View.VISIBLE
                    if (listGroupMemberDetail.isNotEmpty()) {
                        videoHolder.textViewSenderName.visibility = View.VISIBLE
                        videoHolder.textViewSenderName.text = listGroupMemberDetail[0].username
                    } else {
                        videoHolder.textViewSenderName.visibility = View.VISIBLE
                        videoHolder.textViewSenderName.text = "Not your Friend"
                    }
                    if (listChat[position].checkForwarded) {
                        videoHolder.textViewForwardedIn.visibility = View.VISIBLE
                    } else {
                        videoHolder.textViewForwardedIn.visibility = View.GONE
                    }
                    if (null != pickImageFromStorage(
                            context,
                            listChat[position].messageId,
                            2
                        )
                    ) {
                        Glide.with(context).setDefaultRequestOptions(requestOptionsTv()!!)
                            .load(
                                pickImageFromStorage(
                                    context,
                                    listChat[position].messageId,
                                    2
                                )
                            )
                            .into(videoHolder.imageViewReceivedVideo)
                    } else {
                        Glide.with(context)
                            .setDefaultRequestOptions(requestOptionsTv()!!)
                            .load(listChat[position].uri)
                            .into(videoHolder.imageViewReceivedVideo)
                    }


                    videoHolder.layoutReceivedVideo.setOnClickListener(object :
                        View.OnClickListener {
                        override fun onClick(v: View?) {
                            chatClickOptions.videoOptions(listChat[videoHolder.adapterPosition])
                        }
                    })
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    videoHolder.textViewReceivedVideoTime.text = getTime(mTime)
                }
            }
            ChatMessageTypes.IMAGE.type.toInt() -> {
                val imageHolder = holder as ImageViewHolder
                Log.e(
                    "Nive ",
                    "onBindViewHolder:Image Message Status " + listChat[position].messageStatus
                )
                if ("null" == listChat[position].uri) {
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID) && listChat[position].messageStatus == ChatMessageStatus.RETRY.ordinal) {
                        imageHolder.progressBarSentImage.visibility = View.GONE
                        imageHolder.layoutSentImageRetry.visibility = View.VISIBLE
                    } else {
                        Log.e("Nive ", "onBindViewHolder:Image Null ")
                        imageHolder.layoutSentImageRetry.visibility = View.GONE
                        imageHolder.progressBarSentImage.visibility = View.VISIBLE
                    }
                    imageHolder.progressBarReceivedImage.visibility = View.VISIBLE
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                        imageHolder.textViewImageSentTime.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_message_not_deliver,
                            0
                        )
                    }
                } else {
                    imageHolder.layoutSentImageRetry.visibility = View.GONE
                    imageHolder.progressBarSentImage.visibility = View.GONE
                    imageHolder.progressBarReceivedImage.visibility = View.GONE
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                        when (listChat[position].messageStatus) {
                            ChatMessageStatus.NOT_SENT.ordinal -> {
                                imageHolder.textViewImageSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_not_deliver,
                                    0
                                )
                            }
                            ChatMessageStatus.SENT.ordinal -> {
                                imageHolder.textViewImageSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_send,
                                    0
                                )
                            }
                            ChatMessageStatus.DELIVERED.ordinal -> {
                                imageHolder.textViewImageSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_delivered,
                                    0
                                )
                            }
                            ChatMessageStatus.READ.ordinal -> {
                                imageHolder.textViewImageSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_read,
                                    0
                                )
                            }
                        }
                    }
                }
                if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                    imageHolder.layoutReceiveImage.visibility = View.GONE
                    imageHolder.layoutSentImage.visibility = View.VISIBLE
                    if (listChat[position].checkForwarded) {
                        imageHolder.textViewForwardedOut.visibility = View.VISIBLE
                    } else {
                        imageHolder.textViewForwardedOut.visibility = View.GONE
                    }

                    if (null != pickImageFromStorage(
                            context,
                            listChat[position].messageId,
                            1
                        )
                    ) {
                        Glide.with(context)
                            .setDefaultRequestOptions(requestOptionsT()!!)
                            .load(
                                pickImageFromStorage(
                                    context,
                                    listChat[position].messageId,
                                    1
                                )
                            )
                            .into(imageHolder.imageViewSentImage)
                    } else {
                        Glide.with(context)
                            .setDefaultRequestOptions(requestOptionsT()!!)
                            .load(listChat[position].uri)
                            .into(imageHolder.imageViewSentImage)
                    }
                    imageHolder.layoutSentImage.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            chatClickOptions.imageOptions(listChat[imageHolder.adapterPosition])
                        }
                    })
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    imageHolder.textViewImageSentTime.text = getTime(mTime)
                } else {
                    imageHolder.layoutSentImage.visibility = View.GONE
                    imageHolder.layoutReceiveImage.visibility = View.VISIBLE
                    if (listGroupMemberDetail.isNotEmpty()) {
                        imageHolder.textViewSenderName.visibility = View.VISIBLE
                        imageHolder.textViewSenderName.text = listGroupMemberDetail[0].username
                    } else {
                        imageHolder.textViewSenderName.visibility = View.VISIBLE
                        imageHolder.textViewSenderName.text = "Not your Friend"
                    }
                    if (listChat[position].checkForwarded) {
                        imageHolder.textViewForwardedIn.visibility = View.VISIBLE
                    } else {
                        imageHolder.textViewForwardedIn.visibility = View.GONE
                    }

                    if (null != pickImageFromStorage(
                            context,
                            listChat[position].messageId,
                            1
                        )
                    ) {
                        Glide.with(context).setDefaultRequestOptions(requestOptionsT()!!)
                            .load(
                                pickImageFromStorage(
                                    context,
                                    listChat[position].messageId,
                                    1
                                )
                            )
                            .into(imageHolder.imageViewReceivedImage)
                    } else {
                        Glide.with(context)
                            .setDefaultRequestOptions(requestOptionsT()!!)
                            .load(listChat[position].uri)
                            .into(imageHolder.imageViewReceivedImage)
                    }


                    imageHolder.layoutReceiveImage.setOnClickListener(object :
                        View.OnClickListener {
                        override fun onClick(v: View?) {
                            chatClickOptions.imageOptions(listChat[imageHolder.adapterPosition])
                        }
                    })
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    imageHolder.textViewImageReceivedTime.text = getTime(mTime)
                }
            }
            ChatMessageTypes.DOCUMENT.type.toInt() -> {
                val documentHolder = holder as DocumentViewHolder
                if ("null" == listChat[position].uri) {
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID) && listChat[position].messageStatus == ChatMessageStatus.RETRY.ordinal) {
                        documentHolder.layoutSentDocumentRetry.visibility = View.VISIBLE
                    } else {
                        documentHolder.layoutSentDocumentRetry.visibility = View.GONE
                    }
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                        documentHolder.textViewSentDocumentTime.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_message_not_deliver,
                            0
                        )
                    }
                } else {
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                        when (listChat[position].messageStatus) {
                            ChatMessageStatus.NOT_SENT.ordinal -> {
                                documentHolder.textViewSentDocumentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_not_deliver,
                                    0
                                )
                            }
                            ChatMessageStatus.SENT.ordinal -> {
                                documentHolder.textViewSentDocumentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_send,
                                    0
                                )
                            }
                            ChatMessageStatus.DELIVERED.ordinal -> {
                                documentHolder.textViewSentDocumentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_delivered,
                                    0
                                )
                            }
                            ChatMessageStatus.READ.ordinal -> {
                                documentHolder.textViewSentDocumentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_read,
                                    0
                                )
                            }
                        }
                    }
                }
                if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                    documentHolder.layoutReceivedDocument.visibility = View.GONE
                    documentHolder.layoutSentDocument.visibility = View.VISIBLE
                    if (listChat[position].checkForwarded) {
                        documentHolder.textViewForwardedOut.visibility = View.VISIBLE
                    } else {
                        documentHolder.textViewForwardedOut.visibility = View.GONE
                    }

                    documentHolder.textViewSentDocumentName.text = listChat[position].message
                    documentHolder.layoutSentDocument.setOnClickListener(object :
                        View.OnClickListener {
                        override fun onClick(v: View?) {
                            chatClickOptions.documentOptions(listChat[position])
                        }
                    })
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    documentHolder.textViewSentDocumentTime.text = getTime(mTime)
                } else {
                    documentHolder.layoutSentDocument.visibility = View.GONE
                    documentHolder.layoutReceivedDocument.visibility = View.VISIBLE
                    if (listGroupMemberDetail.isNotEmpty()) {
                        documentHolder.textViewSenderName.visibility = View.VISIBLE
                        documentHolder.textViewSenderName.text = listGroupMemberDetail[0].username
                    } else {
                        documentHolder.textViewSenderName.visibility = View.VISIBLE
                        documentHolder.textViewSenderName.text = "Not your Friend"
                    }
                    if (listChat[position].checkForwarded) {
                        documentHolder.textViewForwardedIn.visibility = View.VISIBLE
                    } else {
                        documentHolder.textViewForwardedIn.visibility = View.GONE
                    }

                    documentHolder.textViewReceivedDocumentName.text = listChat[position].message
                    documentHolder.layoutReceivedDocument.setOnClickListener(object :
                        View.OnClickListener {
                        override fun onClick(v: View?) {
                            chatClickOptions.documentOptions(listChat[position])
                        }
                    })
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    documentHolder.textViewReceivedDocumentTime.text = getTime(mTime)
                }
            }
            ChatMessageTypes.GIF.type.toInt() -> {
                val gifHolder = holder as ImageViewHolder
                if ("null" == listChat[position].uri) {
                    gifHolder.progressBarSentImage.visibility = View.VISIBLE
                    gifHolder.progressBarReceivedImage.visibility = View.VISIBLE
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                        gifHolder.textViewImageSentTime.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_message_not_deliver,
                            0
                        )
                    }
                } else {
                    gifHolder.progressBarSentImage.visibility = View.GONE
                    gifHolder.progressBarReceivedImage.visibility = View.GONE
                    if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                        when (listChat[position].messageStatus) {
                            ChatMessageStatus.NOT_SENT.ordinal -> {
                                gifHolder.textViewImageSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_not_deliver,
                                    0
                                )
                            }
                            ChatMessageStatus.SENT.ordinal -> {
                                gifHolder.textViewImageSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_send,
                                    0
                                )
                            }
                            ChatMessageStatus.DELIVERED.ordinal -> {
                                gifHolder.textViewImageSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_delivered,
                                    0
                                )
                            }
                            ChatMessageStatus.READ.ordinal -> {
                                gifHolder.textViewImageSentTime.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_message_read,
                                    0
                                )
                            }
                        }
                    }
                }

                if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                    gifHolder.layoutReceiveImage.visibility = View.GONE
                    gifHolder.layoutSentImage.visibility = View.VISIBLE
                    if (listChat[position].checkForwarded) {
                        gifHolder.textViewForwardedOut.visibility = View.VISIBLE
                    } else {
                        gifHolder.textViewForwardedOut.visibility = View.GONE
                    }

                    Glide.with(context)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .asGif()
                        .apply(RequestOptions().set(GifOptions.DISABLE_ANIMATION, false))
                        .load(listChat[position].uri)
                        .into(gifHolder.imageViewSentImage)
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    gifHolder.textViewImageSentTime.text = getTime(mTime)
                } else {
                    gifHolder.layoutSentImage.visibility = View.GONE
                    gifHolder.layoutReceiveImage.visibility = View.VISIBLE
                    if (listGroupMemberDetail.isNotEmpty()) {
                        gifHolder.textViewSenderName.visibility = View.VISIBLE
                        gifHolder.textViewSenderName.text = listGroupMemberDetail[0].username
                    } else {
                        gifHolder.textViewSenderName.visibility = View.VISIBLE
                        gifHolder.textViewSenderName.text = "Not your Friend"
                    }
                    if (listChat[position].checkForwarded) {
                        gifHolder.textViewForwardedIn.visibility = View.VISIBLE
                    } else {
                        gifHolder.textViewForwardedIn.visibility = View.GONE
                    }

                    Glide.with(context)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .asGif()
                        .apply(RequestOptions().set(GifOptions.DISABLE_ANIMATION, false))
                        .load(listChat[position].uri)
                        .into(gifHolder.imageViewReceivedImage)
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    gifHolder.textViewImageReceivedTime.text = getTime(mTime)
                }
            }
            ChatMessageTypes.LOCATION.type.toInt() -> {
                val locationHolder = holder as LocationViewHolder
                var mMapUrl: String = ""
                val locationData =
                    mAppDatabase!!.getLocationDao()
                        .getLocationMessage(
                            listChat[position].messageId,
                            SharedPreferenceEditor.getData(USER_ID)!!
                        )
                if (locationData.isNotEmpty()) {
                    mMapUrl =
                        getStaticMap(
                            context, listChat[position].latitude.toString()
                                .plus(",")
                                .plus(listChat[position].longitude.toString())
                        )!!
                }
                /* val mMapUrl: String = getStaticMap(context, listChat[position].latitude.toString()
                     .plus(",")
                     .plus(listChat[position].longitude.toString()))!!*/
                if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                    locationHolder.layoutReceivedLocation.visibility = View.GONE
                    locationHolder.layoutSentLocation.visibility = View.VISIBLE
                    if (listChat[position].checkForwarded) {
                        locationHolder.textViewForwardedOut.visibility = View.VISIBLE
                    } else {
                        locationHolder.textViewForwardedOut.visibility = View.GONE
                    }

                    Glide.with(context)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .load(mMapUrl)
                        .into(locationHolder.imageViewSentLocation)
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    locationHolder.textViewSentLocationTime.text = getTime(mTime)
                    when (listChat[position].messageStatus) {
                        ChatMessageStatus.NOT_SENT.ordinal -> {
                            locationHolder.textViewSentLocationTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_not_deliver,
                                0
                            )
                        }
                        ChatMessageStatus.SENT.ordinal -> {
                            locationHolder.textViewSentLocationTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_send,
                                0
                            )
                        }
                        ChatMessageStatus.DELIVERED.ordinal -> {
                            locationHolder.textViewSentLocationTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_delivered,
                                0
                            )
                        }
                        ChatMessageStatus.READ.ordinal -> {
                            locationHolder.textViewSentLocationTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_read,
                                0
                            )
                        }
                    }
                    locationHolder.layoutSentLocation.setOnClickListener(object :
                        View.OnClickListener {
                        override fun onClick(v: View?) {
                            chatClickOptions.locationOptions(listChat[locationHolder.adapterPosition])
                        }
                    })
                } else {
                    locationHolder.layoutSentLocation.visibility = View.GONE
                    locationHolder.layoutReceivedLocation.visibility = View.VISIBLE
                    if (listGroupMemberDetail.isNotEmpty()) {
                        locationHolder.textViewSenderName.visibility = View.VISIBLE
                        locationHolder.textViewSenderName.text = listGroupMemberDetail[0].username
                    } else {
                        locationHolder.textViewSenderName.visibility = View.VISIBLE
                        locationHolder.textViewSenderName.text = "Not your Friend"
                    }
                    if (listChat[position].checkForwarded) {
                        locationHolder.textViewForwardedIn.visibility = View.VISIBLE
                    } else {
                        locationHolder.textViewForwardedIn.visibility = View.GONE
                    }

                    Glide.with(context)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .load(mMapUrl)
                        .into(locationHolder.imageViewReceivedLocation)
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    locationHolder.textViewReceivedLocationTime.text = getTime(mTime)

                    locationHolder.layoutReceivedLocation.setOnClickListener(object :
                        View.OnClickListener {
                        override fun onClick(v: View?) {
                            chatClickOptions.locationOptions(listChat[locationHolder.adapterPosition])
                        }
                    })
                }
            }
            ChatMessageTypes.DATE.type.toInt() -> {
                val chatDateHolder = holder as ChatDateViewHolder
                chatDateHolder.textViewDate.text =
                    getSmsTodayYestFromMilli(listChat[position].chatTime.toLong())
            }
            ChatMessageTypes.UNREAD.type.toInt() -> {
                val chatUnreadHolder = holder as ChatUnreadViewHolder
                if (unreadMessageCount == 1) {
                    chatUnreadHolder.textViewUnread.text =
                        unreadMessageCount.toString().plus(" ")
                            .plus(context.resources.getString(R.string.str_unread_message))
                } else {
                    chatUnreadHolder.textViewUnread.text =
                        unreadMessageCount.toString().plus(" ")
                            .plus(context.resources.getString(R.string.str_unread_messages))
                }
            }
            REPLY -> {
                val replyViewHolder = holder as ReplyViewHolder
                val listChatModel =
                    mAppDatabase!!.getChatDao()
                        .getGroupChatData(
                            listChat[position].replyMessageId!!,
                            SharedPreferenceEditor.getData(USER_ID)!!
                        )
                if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                    replyViewHolder.layoutReceiverReply.visibility = View.GONE
                    replyViewHolder.textViewReceiverReplyMessageTime.visibility = View.GONE
                    replyViewHolder.layoutSenderReply.visibility = View.VISIBLE
                    replyViewHolder.textViewSenderReplyMessageTime.visibility = View.VISIBLE

                    if (listChat[position].checkEdit) {
                        replyViewHolder.imageViewEdited.visibility = View.VISIBLE
                    } else {
                        replyViewHolder.imageViewEdited.visibility = View.GONE
                    }

                    replyViewHolder.textViewSenderReplyMessage.text =
                        listChat[position].message.plus(context.resources.getString(R.string.string_chat_spaces))
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    replyViewHolder.textViewSenderReplyMessageTime.text = getTime(mTime)

                    if (listChatModel.isNotEmpty()) {
                        if (listChatModel[0].sender == SharedPreferenceEditor.getData(USER_ID)) {
                            replyViewHolder.textViewReplierName.text =
                                context.resources.getString(R.string.str_you)
                        } else {
                            val userDetailsModel = mAppDatabase!!.getUserDetailsDao()
                                .getUserDetails(listChatModel[0].sender)
                            if (userDetailsModel.isNotEmpty()) {
                                replyViewHolder.textViewReplierName.text =
                                    userDetailsModel[0].username
                            }
                        }
                        if (listChatModel[0].messageType == ChatMessageTypes.TEXT.type) {
                            replyViewHolder.imageViewSenderReply.visibility = View.GONE
                            replyViewHolder.textViewSenderReply.text = listChatModel[0].message
                        } else if (listChatModel[0].messageType == ChatMessageTypes.AUDIO.type) {
                            replyViewHolder.imageViewSenderReply.visibility = View.GONE
                            replyViewHolder.textViewSenderReply.text =
                                getLastMessage(listChatModel[0].messageType)
                        } else if (listChatModel[0].messageType == ChatMessageTypes.IMAGE.type ||
                            listChatModel[0].messageType == ChatMessageTypes.VIDEO.type ||
                            listChatModel[0].messageType == ChatMessageTypes.GIF.type
                        ) {
                            replyViewHolder.imageViewSenderReply.visibility = View.VISIBLE
                            when (listChatModel[0].messageType) {
                                ChatMessageTypes.VIDEO.type -> {
                                    Glide.with(context)
                                        .setDefaultRequestOptions(requestOptionsTv()!!)
                                        .load(listChatModel[0].uri)
                                        .into(replyViewHolder.imageViewSenderReply)
                                }
                                ChatMessageTypes.GIF.type -> {
                                    Glide.with(context)
                                        .setDefaultRequestOptions(requestOptionsT()!!)
                                        .asGif()
                                        .apply(
                                            RequestOptions().set(
                                                GifOptions.DISABLE_ANIMATION,
                                                false
                                            )
                                        )
                                        .load(listChatModel[0].uri)
                                        .into(replyViewHolder.imageViewSenderReply)
                                }
                                else -> {
                                    Glide.with(context)
                                        .setDefaultRequestOptions(requestOptionsT()!!)
                                        .load(listChatModel[0].uri)
                                        .into(replyViewHolder.imageViewSenderReply)
                                }
                            }
                            replyViewHolder.textViewSenderReply.text =
                                getLastMessage(listChatModel[0].messageType)
                        } else if (listChatModel[0].messageType == ChatMessageTypes.LOCATION.type ||
                            listChatModel[0].messageType == ChatMessageTypes.LIVELOCATION.type
                        ) {
                            replyViewHolder.imageViewSenderReply.visibility = View.VISIBLE
                            val mMapUrlReply =
                                getStaticMap(
                                    context, listChatModel[0].latitude.toString()
                                        .plus(",")
                                        .plus(listChatModel[0].longitude.toString())
                                )
                            Glide.with(context)
                                .setDefaultRequestOptions(requestOptionsT()!!)
                                .load(mMapUrlReply)
                                .into(replyViewHolder.imageViewSenderReply)
                            replyViewHolder.textViewSenderReply.text =
                                getLastMessage(listChatModel[0].messageType)
                        } else if (listChatModel[0].messageType == ChatMessageTypes.DOCUMENT.type) {
                            replyViewHolder.imageViewSenderReply.visibility = View.VISIBLE
                            Glide.with(context)
                                .load(R.drawable.ic_document)
                                .into(replyViewHolder.imageViewSenderReply)
                            replyViewHolder.textViewSenderReply.text =
                                getLastMessage(listChatModel[0].messageType)
                        } else {
                            replyViewHolder.imageViewSenderReply.visibility = View.GONE
                            replyViewHolder.textViewSenderReply.text =
                                context.resources.getString(R.string.str_reply)
                        }
                    } else {
                        replyViewHolder.imageViewSenderReply.visibility = View.GONE
                        replyViewHolder.textViewSenderReply.text =
                            context.resources.getString(R.string.str_deleted)
                    }

                    replyViewHolder.layoutSenderReplyUI.setOnClickListener(object :
                        View.OnClickListener {
                        override fun onClick(p0: View?) {
                            /*chatClickOptions.replyRedirection(listChat[position].replyMessageId!!)*/
                            chatClickOptions.replyRedirection(listChat[replyViewHolder.adapterPosition].replyMessageId!!)
                        }
                    })

                    when (listChat[position].messageStatus) {
                        ChatMessageStatus.NOT_SENT.ordinal -> {
                            replyViewHolder.textViewSenderReplyMessageTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_not_deliver,
                                0
                            )
                        }
                        ChatMessageStatus.SENT.ordinal -> {
                            replyViewHolder.textViewSenderReplyMessageTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_send,
                                0
                            )
                        }
                        ChatMessageStatus.DELIVERED.ordinal -> {
                            replyViewHolder.textViewSenderReplyMessageTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_delivered,
                                0
                            )
                        }
                        ChatMessageStatus.READ.ordinal -> {
                            replyViewHolder.textViewSenderReplyMessageTime.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_message_read,
                                0
                            )
                        }
                    }
                } else {
                    replyViewHolder.layoutSenderReply.visibility = View.GONE
                    replyViewHolder.textViewSenderReplyMessageTime.visibility = View.GONE
                    replyViewHolder.layoutReceiverReply.visibility = View.VISIBLE
                    replyViewHolder.textViewReceiverReplyMessageTime.visibility = View.VISIBLE

                    if (listChat[position].checkEdit) {
                        replyViewHolder.imageViewReceiverEdited.visibility = View.VISIBLE
                    } else {
                        replyViewHolder.imageViewReceiverEdited.visibility = View.GONE
                    }

                    if (listGroupMemberDetail.isNotEmpty()) {
                        replyViewHolder.textViewSenderName.visibility = View.VISIBLE
                        replyViewHolder.textViewSenderName.text = listGroupMemberDetail[0].username
                    }
                    replyViewHolder.textViewReceiverReplyMessage.text =
                        listChat[position].message.plus(context.resources.getString(R.string.string_chat_spaces_receiver))
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    replyViewHolder.textViewReceiverReplyMessageTime.text = getTime(mTime)

                    if (listChatModel.isNotEmpty()) {
                        if (listChatModel[0].sender == SharedPreferenceEditor.getData(USER_ID)) {
                            replyViewHolder.textViewReplierNameIn.text =
                                context.resources.getString(R.string.str_you)
                        } else {
                            val userDetailsModel = mAppDatabase!!.getUserDetailsDao()
                                .getUserDetails(listChatModel[0].sender)
                            if (userDetailsModel.isNotEmpty()) {
                                replyViewHolder.textViewReplierNameIn.text =
                                    userDetailsModel[0].username
                            }
                        }
                        if (listChatModel[0].messageType == ChatMessageTypes.TEXT.type) {
                            replyViewHolder.imageViewReceiverReply.visibility = View.GONE
                            replyViewHolder.textViewReceiverReply.text = listChatModel[0].message
                        } else if (listChatModel[0].messageType == ChatMessageTypes.AUDIO.type) {
                            replyViewHolder.imageViewReceiverReply.visibility = View.GONE
                            replyViewHolder.textViewReceiverReply.text =
                                getLastMessage(listChatModel[0].messageType)
                        } else if (listChatModel[0].messageType == ChatMessageTypes.IMAGE.type ||
                            listChatModel[0].messageType == ChatMessageTypes.VIDEO.type ||
                            listChatModel[0].messageType == ChatMessageTypes.GIF.type
                        ) {
                            replyViewHolder.imageViewReceiverReply.visibility = View.VISIBLE
                            when (listChatModel[0].messageType) {
                                ChatMessageTypes.VIDEO.type -> {
                                    Glide.with(context)
                                        .setDefaultRequestOptions(requestOptionsTv()!!)
                                        .load(listChatModel[0].uri)
                                        .into(replyViewHolder.imageViewReceiverReply)
                                }
                                ChatMessageTypes.GIF.type -> {
                                    Glide.with(context)
                                        .setDefaultRequestOptions(requestOptionsT()!!)
                                        .asGif()
                                        .apply(
                                            RequestOptions().set(
                                                GifOptions.DISABLE_ANIMATION,
                                                false
                                            )
                                        )
                                        .load(listChatModel[0].uri)
                                        .into(replyViewHolder.imageViewReceiverReply)
                                }
                                else -> {
                                    Glide.with(context)
                                        .setDefaultRequestOptions(requestOptionsT()!!)
                                        .load(listChatModel[0].uri)
                                        .into(replyViewHolder.imageViewReceiverReply)
                                }
                            }
                            replyViewHolder.textViewReceiverReply.text =
                                getLastMessage(listChatModel[0].messageType)
                        } else if (listChatModel[0].messageType == ChatMessageTypes.LOCATION.type ||
                            listChatModel[0].messageType == ChatMessageTypes.LIVELOCATION.type
                        ) {
                            replyViewHolder.imageViewReceiverReply.visibility = View.VISIBLE
                            val mMapUrlReply =
                                getStaticMap(
                                    context, listChatModel[0].latitude.toString()
                                        .plus(",")
                                        .plus(listChatModel[0].longitude.toString())
                                )
                            Glide.with(context)
                                .setDefaultRequestOptions(requestOptionsT()!!)
                                .load(mMapUrlReply)
                                .into(replyViewHolder.imageViewReceiverReply)
                            replyViewHolder.textViewReceiverReply.text =
                                getLastMessage(listChatModel[0].messageType)
                        } else if (listChatModel[0].messageType == ChatMessageTypes.DOCUMENT.type) {
                            replyViewHolder.imageViewReceiverReply.visibility = View.VISIBLE
                            Glide.with(context)
                                .load(R.drawable.ic_document)
                                .into(replyViewHolder.imageViewReceiverReply)
                            replyViewHolder.textViewReceiverReply.text =
                                getLastMessage(listChatModel[0].messageType)
                        } else {
                            replyViewHolder.imageViewReceiverReply.visibility = View.GONE
                            replyViewHolder.textViewReceiverReply.text =
                                context.resources.getString(R.string.str_reply)
                        }
                    } else {
                        replyViewHolder.imageViewReceiverReply.visibility = View.GONE
                        replyViewHolder.textViewReceiverReply.text =
                            context.resources.getString(R.string.str_deleted)
                    }

                    replyViewHolder.layoutReceiverReplyUI.setOnClickListener(object :
                        View.OnClickListener {
                        override fun onClick(p0: View?) {
                            chatClickOptions.replyRedirection(listChat[holder.adapterPosition].replyMessageId!!)
                        }
                    })
                }
            }
            ChatMessageTypes.CREATEGROUP.type.toInt(),
            ChatMessageTypes.GROUPINFO.type.toInt(),
            ChatMessageTypes.ADDMEMBER.type.toInt(),
            ChatMessageTypes.REMOVEMEMBER.type.toInt(),
            ChatMessageTypes.EXITMEMBER.type.toInt() -> {
                val groupDescriptionViewHolder = holder as GroupDescriptionViewHolder
                var mAppendData: String = ""
                mAppendData = when {
                    listChat[position].sender == SharedPreferenceEditor.getData(USER_ID) -> {
                        context.resources.getString(R.string.str_you)
                    }
                    listGroupMemberDetail.isNotEmpty() -> ({
                        listGroupMemberDetail[0].username
                    }).toString()
                    else -> {
                        ""
                    }
                }

                if (listChat[position].messageType == ChatMessageTypes.ADDMEMBER.type) {
                    var mMemberName = ""
                    val mIds = listChat[position].message.replace("added ", "")
                    val mMemberIds = mIds.split(",").toTypedArray()
                    if (mMemberIds.isNotEmpty()) {
                        for (i in 0 until mMemberIds.size) {
                            if (mMemberIds[i] == SharedPreferenceEditor.getData(USER_ID)) {
                                mMemberName = "You"
                            } else {
                                val mGroupMemberDetails =
                                    mAppDatabase!!.getUserDetailsDao().getUserDetails(mMemberIds[i])
                                if (mGroupMemberDetails.isNotEmpty()) {
                                    var mOtherName = ""
                                    for (j in mGroupMemberDetails.indices) {
                                        mGroupMemberDetails[j].username?.let {
                                            mOtherName = it
                                        }
                                    }
                                    mMemberName = mMemberName.plus(mOtherName)
                                }
                            }
                            if (i != mMemberIds.size - 1) {
                                mMemberName = mMemberName.plus(",")
                            }
                        }
                        groupDescriptionViewHolder.textViewGroupDescription.text =
                            mAppendData.plus(" ").plus("added").plus(" ")
                                .plus(mMemberName)
                    }
                } else if (listChat[position].messageType == ChatMessageTypes.REMOVEMEMBER.type) {
                    var mMemberName = ""
                    val mIds = listChat[position].message.replace("removed ", "")
                    val mMemberIds = mIds.split(",").toTypedArray()
                    if (mMemberIds.isNotEmpty()) {
                        for (i in mMemberIds.indices) {
                            if (mMemberIds[i] == SharedPreferenceEditor.getData(USER_ID)) {
                                mMemberName = "You"
                            } else {
                                val mGroupMemberDetails =
                                    mAppDatabase!!.getUserDetailsDao().getUserDetails(mMemberIds[i])
                                if (mGroupMemberDetails.isNotEmpty()) {
                                    var mOtherName = ""
                                    for (j in mGroupMemberDetails.indices) {
                                        mGroupMemberDetails[j].username?.let {
                                            mOtherName = it
                                        }
                                    }
                                    mMemberName = mMemberName.plus(mOtherName)
                                }
                            }
                            if (i != mMemberIds.size - 1) {
                                mMemberName = mMemberName.plus(",")
                            }
                        }
                        groupDescriptionViewHolder.textViewGroupDescription.text =
                            mAppendData.plus(" ").plus("removed").plus(" ").plus(mMemberName)
                    }
                } else {
                    groupDescriptionViewHolder.textViewGroupDescription.text =
                        mAppendData.plus(" ").plus(listChat[position].message)
                }
            }
            ChatMessageTypes.GROUPPROFILE.type.toInt() -> {
                val groupProfileViewHolder = holder as GroupProfileViewHolder
                var mAppendData: String = ""
                mAppendData = when {
                    listChat[position].sender == SharedPreferenceEditor.getData(USER_ID) -> {
                        context.resources.getString(R.string.str_you)
                    }
                    listGroupMemberDetail.isNotEmpty() -> ({
                        listGroupMemberDetail[0].username
                    }).toString()
                    else -> {
                        ""
                    }
                }
                val mGroupProfile = listChat[position].message.split(" to ")
                Glide.with(context)
                    .setDefaultRequestOptions(requestOptionsD()!!)
                    .load(mGroupProfile[0])
                    .into(groupProfileViewHolder.imageViewGroupProfileBefore)
                Glide.with(context)
                    .setDefaultRequestOptions(requestOptionsD()!!)
                    .load(mGroupProfile[1])
                    .into(groupProfileViewHolder.imageViewGroupProfileAfter)
                groupProfileViewHolder.textViewGroupProfileChange.text =
                    mAppendData.plus(" ").plus("changed this group's icon")
            }
            ChatMessageTypes.DELETEFOREVERYONE.type.toInt() -> {
                val deletedMessageViewHolder = holder as DeletedMessageViewHolder
                if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
                    deletedMessageViewHolder.layoutInMessage.visibility = View.GONE
                    deletedMessageViewHolder.layoutOutMessage.visibility = View.VISIBLE
                    deletedMessageViewHolder.textViewOutDeletedMessage.text =
                        context.resources.getString(R.string.str_you_deleted_this_message)
                            .plus(context.resources.getString(R.string.string_chat_spaces))
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    deletedMessageViewHolder.textViewOutMessageTime.text = getTime(mTime)
                } else {
                    deletedMessageViewHolder.layoutOutMessage.visibility = View.GONE
                    deletedMessageViewHolder.layoutInMessage.visibility = View.VISIBLE
                    if (listGroupMemberDetail.isNotEmpty()) {
                        deletedMessageViewHolder.textViewSenderName.visibility = View.VISIBLE
                        deletedMessageViewHolder.textViewSenderName.text =
                            listGroupMemberDetail[0].username
                    } else {
                        deletedMessageViewHolder.textViewSenderName.visibility = View.VISIBLE
                        deletedMessageViewHolder.textViewSenderName.text = "Not your Friend"
                    }
                    deletedMessageViewHolder.textViewInMessage.text =
                        context.resources.getString(R.string.str_deleted_this_message)
                            .plus(context.resources.getString(R.string.string_chat_spaces_receiver))
                    val mTime = getLocalTime(listChat[position].chatTime.toLong())
                    deletedMessageViewHolder.textViewInMessageTime.text = getTime(mTime)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listChat.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SimpleDateFormat")
    override fun getItemViewType(position: Int): Int {
        return if (listChat[position].checkReply &&
            listChat[position].messageType == ChatMessageTypes.TEXT.type
        ) {
            REPLY
        } else {
            val viewtype: Int = listChat[position].messageType.toInt()
            viewtype
        }
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutParent: RelativeLayout =
            itemView.findViewById(R.id.layout_chat_message)
        val layoutOutMessage: RelativeLayout =
            itemView.findViewById(R.id.layout_out_message)
        val textViewForwardedOut: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_out)
        val imageViewEdited: AppCompatImageView =
            itemView.findViewById(R.id.imageview_edited)
        val textViewOutMessage: AppCompatTextView =
            itemView.findViewById(R.id.textview_out_message)
        val textViewOutMessageTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_out_message_time)
        val layoutInMessage: RelativeLayout =
            itemView.findViewById(R.id.layout_in_message)
        val textViewForwardedIn: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_in)
        val imageViewEditedIn: AppCompatImageView =
            itemView.findViewById(R.id.imageview_receiver_edited)
        val textViewSenderName: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_name)
        val textViewInMessage: AppCompatTextView =
            itemView.findViewById(R.id.textview_in_message)
        val textViewInMessageTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_in_message_time)

        init {
            this.setIsRecyclable(true)
        }
    }

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutParentAudio: LinearLayout =
            itemView.findViewById(R.id.ll_item_audio)
        val layoutAudioSent: LinearLayout =
            itemView.findViewById(R.id.layout_send_audio)
        val textViewForwardedOut: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_out)
        val imageViewMicSentAudio: AppCompatImageView =
            itemView.findViewById(R.id.imageview_mic_send)
        val imageViewPlaySentAudio: AppCompatImageView =
            itemView.findViewById(R.id.imageview_play_send)
        val imageViewPauseSentAudio: AppCompatImageView =
            itemView.findViewById(R.id.imageview_pause_send)
        val seekarBarSentAudio: SeekBar =
            itemView.findViewById(R.id.seekbar_out)
        val textViewSentAudioRunTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_runtime_out)
        val textViewSentAudioTotalTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_total_time_out)
        val textViewAudioSentTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_audio_time_out)
        val layoutSentAudioRetry: ConstraintLayout =
            itemView.findViewById(R.id.rl_sender_retry_audio)
        private val textViewSenderRetry: AppCompatTextView =
            itemView.findViewById(R.id.tv_sender_retry_audio)
        val layoutAudioReceived: LinearLayout =
            itemView.findViewById(R.id.layout_receive_audio)
        val textViewForwardedIn: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_in)
        val imageViewMicReceivedAudio: AppCompatImageView =
            itemView.findViewById(R.id.imageview_mic_receive_audio)
        val imageViewPlayReceivedAudio: AppCompatImageView =
            itemView.findViewById(R.id.imageview_play_in)
        val imageViewPauseReceivedAudio: AppCompatImageView =
            itemView.findViewById(R.id.imageview_pause_in)
        val seekarBarReceivedAudio: SeekBar =
            itemView.findViewById(R.id.seekbar_in)
        val textViewReceivedAudioRunTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_runtime_in)
        val textViewReceivedAudioTotalTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_total_time_in)
        val textViewAudioReceivedTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_audio_time_in)
        val textViewSenderName: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_name)

        init {
            this.setIsRecyclable(true)
            textViewSenderRetry.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    chatClickOptions.retryOptions(listChat[adapterPosition])
                }
            })
        }
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutVideoParent: RelativeLayout = itemView.findViewById(R.id.layout_video_parent)
        val layoutSentVideo: RelativeLayout = itemView.findViewById(R.id.layout_sent_video)
        val textViewForwardedOut: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_out)
        val imageViewSentVideo: AppCompatImageView =
            itemView.findViewById(R.id.imgaeview_sent_video)
        val layoutCenterViewVideo: RelativeLayout =
            itemView.findViewById(R.id.layout_center_view_video)
        val progressBarSentVideo: ProgressBar = itemView.findViewById(R.id.progress_bar_sent_video)
        val textViewSentVideoTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_sent_video_time)
        val layoutSenderRetry: RelativeLayout =
            itemView.findViewById(R.id.layout_sender_retry_video)
        private val textViewSenderRetryVideo: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_retry_video)
        val layoutReceivedVideo: RelativeLayout = itemView.findViewById(R.id.layout_received_video)
        val textViewForwardedIn: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_in)
        val textViewSenderName: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_name)
        val imageViewReceivedVideo: AppCompatImageView =
            itemView.findViewById(R.id.imageview_received_video)
        val progressBarReceivedVideo: ProgressBar =
            itemView.findViewById(R.id.progress_bar_received_video)
        val textViewReceivedVideoTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_received_video_time)

        init {
            this.setIsRecyclable(true)

            textViewSenderRetryVideo.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    chatClickOptions.retryOptions(listChat[adapterPosition])
                }
            })
        }
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutImageParent: RelativeLayout = itemView.findViewById(R.id.layout_image_parent)
        val layoutSentImage: RelativeLayout = itemView.findViewById(R.id.layout_send_image)
        val textViewForwardedOut: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_out)
        val imageViewSentImage: AppCompatImageView =
            itemView.findViewById(R.id.imageview_send_image)
        val progressBarSentImage: ProgressBar = itemView.findViewById(R.id.progress_bar_send_image)
        val textViewImageSentTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_send_image_time)
        val layoutSentImageRetry: RelativeLayout = itemView.findViewById(R.id.rl_sender_retry_image)
        private val textViewSenderRetry: AppCompatTextView =
            itemView.findViewById(R.id.tv_sender_retry_image)
        val layoutReceiveImage: RelativeLayout = itemView.findViewById(R.id.layout_receive_image)
        val textViewForwardedIn: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_in)
        val textViewSenderName: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_name)
        val imageViewReceivedImage: AppCompatImageView =
            itemView.findViewById(R.id.imageview_receive)
        val progressBarReceivedImage: ProgressBar = itemView.findViewById(R.id.progress_bar_receive)
        val textViewImageReceivedTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_receive_time)

        init {
            this.setIsRecyclable(true)

            textViewSenderRetry.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    chatClickOptions.retryOptions(listChat[adapterPosition])
                }
            })
        }
    }

    inner class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutDocumentParent: LinearLayout = itemView.findViewById(R.id.layout_document_parent)
        val layoutSentDocument: RelativeLayout = itemView.findViewById(R.id.layout_send_document)
        val textViewForwardedOut: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_out)
        val textViewSentDocumentName: AppCompatTextView =
            itemView.findViewById(R.id.textview_send_document_name)
        val textViewSentDocumentTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_send_document_time)
        val layoutSentDocumentRetry: RelativeLayout =
            itemView.findViewById(R.id.rl_sender_retry_document)
        private val textViewSenderDocumentRetry: AppCompatTextView =
            itemView.findViewById(R.id.tv_sender_retry_document)
        val layoutReceivedDocument: LinearLayout =
            itemView.findViewById(R.id.layout_received_document)
        val textViewForwardedIn: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_in)
        val textViewSenderName: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_name)
        val textViewReceivedDocumentName: AppCompatTextView =
            itemView.findViewById(R.id.textview_received_document_name)
        val textViewReceivedDocumentTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_received_document_time)

        init {
            this.setIsRecyclable(true)

            textViewSenderDocumentRetry.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    chatClickOptions.retryOptions(listChat[adapterPosition])
                }
            })
        }
    }

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutLocationParent: RelativeLayout =
            itemView.findViewById(R.id.layout_location_parent)
        val layoutSentLocation: RelativeLayout = itemView.findViewById(R.id.layout_sent_location)
        val textViewForwardedOut: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_out)
        val imageViewSentLocation: AppCompatImageView =
            itemView.findViewById(R.id.imageview_sent_location)
        val textViewSentLocationTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_send_location_time)
        val layoutReceivedLocation: RelativeLayout =
            itemView.findViewById(R.id.layout_received_location)
        val textViewForwardedIn: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_in)
        val textViewSenderName: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_name)
        val imageViewReceivedLocation: AppCompatImageView =
            itemView.findViewById(R.id.imageview_receive_location)
        val textViewReceivedLocationTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_receive_location_time)

        init {
            this.setIsRecyclable(true)
        }
    }

    inner class ChatDateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDate: AppCompatTextView =
            itemView.findViewById(R.id.text_view_date)

        init {
            this.setIsRecyclable(true)
        }
    }

    inner class ChatUnreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewUnread: AppCompatTextView =
            itemView.findViewById(R.id.text_view_unread)

        init {
            this.setIsRecyclable(true)
        }
    }

    inner class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutParent: RelativeLayout =
            itemView.findViewById(R.id.layout_parent)
        val layoutSenderReply: RelativeLayout =
            itemView.findViewById(R.id.layout_sender_reply)
        val imageViewSenderReply: AppCompatImageView =
            itemView.findViewById(R.id.imageview_sender_reply)
        val textViewSenderReply: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_reply)
        val textViewReplierName: AppCompatTextView =
            itemView.findViewById(R.id.textview_replier_name)
        val textViewSenderReplyMessage: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_reply_msg)
        val textViewSenderReplyMessageTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_reply_time)
        val imageViewEdited: AppCompatImageView =
            itemView.findViewById(R.id.imageview_edited)
        val layoutSenderReplyUI: LinearLayout =
            itemView.findViewById(R.id.layout_sender_reply_sent)
        val layoutReceiverReply: RelativeLayout =
            itemView.findViewById(R.id.layout_receiver_reply)
        val textViewForwardedIn: AppCompatTextView =
            itemView.findViewById(R.id.textview_forwarded_in)
        val textViewSenderName: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_name)
        val imageViewReceiverReply: AppCompatImageView =
            itemView.findViewById(R.id.imageview_receiver_reply)
        val textViewReceiverReply: AppCompatTextView =
            itemView.findViewById(R.id.textview_receiver_reply)
        val layoutReceiverReplyUI: RelativeLayout =
            itemView.findViewById(R.id.layout_receiver_reply_in)
        val textViewReplierNameIn: AppCompatTextView =
            itemView.findViewById(R.id.textview_replier_name_in)
        val imageViewReceiverEdited: AppCompatImageView =
            itemView.findViewById(R.id.imageview_receiver_edited)
        val textViewReceiverReplyMessage: AppCompatTextView =
            itemView.findViewById(R.id.textview_receiver_reply_msg)
        val textViewReceiverReplyMessageTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_receiver_reply_time)

        init {
            this.setIsRecyclable(true)
        }
    }

    inner class GroupDescriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewGroupDescription: AppCompatTextView =
            itemView.findViewById(R.id.textview_group_description)

        init {
            this.setIsRecyclable(true)
        }
    }

    inner class GroupProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewGroupProfileBefore: CircleImageView =
            itemView.findViewById(R.id.imageview_group_icon_before)
        val imageViewGroupProfileAfter: CircleImageView =
            itemView.findViewById(R.id.imageview_group_icon_after)
        val textViewGroupProfileChange: AppCompatTextView =
            itemView.findViewById(R.id.textview_group_icon_change)

        init {
            this.setIsRecyclable(true)
        }
    }

    inner class DeletedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutOutMessage: RelativeLayout =
            itemView.findViewById(R.id.layout_out_message)
        val textViewOutDeletedMessage: AppCompatTextView =
            itemView.findViewById(R.id.textview_out_deleted_message)
        val textViewOutMessageTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_out_message_time)
        val layoutInMessage: FrameLayout =
            itemView.findViewById(R.id.layout_in_message)
        val textViewSenderName: AppCompatTextView =
            itemView.findViewById(R.id.textview_sender_name)
        val textViewInMessage: AppCompatTextView =
            itemView.findViewById(R.id.textview_in_deleted_message)
        val textViewInMessageTime: AppCompatTextView =
            itemView.findViewById(R.id.textview_in_message_time)

        init {
            this.setIsRecyclable(true)
        }
    }
}