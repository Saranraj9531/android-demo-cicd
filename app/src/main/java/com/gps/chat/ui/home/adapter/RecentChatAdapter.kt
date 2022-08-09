package com.gps.chat.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gps.chat.R
import com.gps.chat.databinding.ItemConversationBinding
import com.gps.chat.utils.glideRequestOptionProfile
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.ui.chat.model.ChatListModel

class RecentChatAdapter(
    private val context: Context,
    private val listConversation: ArrayList<ChatListModel>,
    private val onItemClicked: ((ChatListModel) -> Unit)
) :
    RecyclerView.Adapter<RecentChatAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val binding =
            ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listConversation.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (listConversation[position].chatType == ChatTypes.SINGLE.type) {
            holder.binding.textviewUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            holder.binding.textviewUsername.text = listConversation[position].name
            Glide.with(context)
                .setDefaultRequestOptions(glideRequestOptionProfile())
                .load(listConversation[position].profilePicture)
                .into(holder.binding.imageviewProfilePicture)
            holder.binding.textviewLastMessage.text = listConversation[position].lastMessage
        } else {
            holder.binding.textviewUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            val listGroupDetails = mAppDatabase!!.getGroupDetailsDao()
                .getGroupDetails(
                    listConversation[position].receiverId,
                    SharedPreferenceEditor.getData(Global.USER_ID)
                )
            if (listGroupDetails.isNotEmpty()) {
                holder.binding.textviewUsername.text = listGroupDetails[0].groupTitle
                Glide.with(context)
                    .setDefaultRequestOptions(glideRequestOptionProfile())
                    .load(listGroupDetails[0].groupImage)
                    .into(holder.binding.imageviewProfilePicture)
            }
            if (listConversation[position].messageType == ChatMessageTypes.ADDMEMBER.type) {
                var mAppendData: String = ""
                if (listConversation[position].sender == SharedPreferenceEditor.getData(Global.USER_ID)) {
                    mAppendData = context.resources.getString(R.string.str_you)
                } else {
                    val listMemberDetail =
                        mAppDatabase!!.getUserDetailsDao()
                            .getUserDetails(listConversation[position].sender)
                    listMemberDetail[0].name?.let {
                        if (listMemberDetail.isNotEmpty()) {
                            mAppendData = it
                        }

                    }
                }
                var mMemberName = ""
                val mIds = listConversation[position].lastMessage.replace("added ", "")
                val mMemberIds = mIds.split(",").toTypedArray()
                if (mMemberIds.isNotEmpty()) {
                    for (i in 0 until mMemberIds.size) {
                        if (mMemberIds[i] == SharedPreferenceEditor.getData(Global.USER_ID)) {
                            mMemberName = "You"
                        } else {
                            val mGroupMemberDetails =
                                mAppDatabase!!.getUserDetailsDao().getUserDetails(mMemberIds[i])
                            if (mGroupMemberDetails.isNotEmpty()) {
                                var mOtherName = ""

                                for (j in mGroupMemberDetails.indices) {
                                    mGroupMemberDetails[j].name?.let {
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
                    holder.binding.textviewLastMessage.text =
                        mAppendData.plus(" ").plus("added").plus(" ")
                            .plus(mMemberName)
                }
            } else if (listConversation[position].messageType == ChatMessageTypes.REMOVEMEMBER.type) {
                var mAppendData: String = ""
                if (listConversation[position].sender == SharedPreferenceEditor.getData(Global.USER_ID)) {
                    mAppendData = context.resources.getString(R.string.str_you)
                } else {
                    val listMemberDetail =
                        mAppDatabase!!.getUserDetailsDao()
                            .getUserDetails(listConversation[position].sender)
                    listMemberDetail[0].name?.let {
                        if (listMemberDetail.isNotEmpty()) {
                            mAppendData = it
                        }

                    }
                }
                var mMemberName = ""
                val mIds = listConversation[position].lastMessage.replace("removed ", "")
                val mMemberIds = mIds.split(",").toTypedArray()
                if (mMemberIds.isNotEmpty()) {
                    for (i in mMemberIds.indices) {
                        if (mMemberIds[i] == SharedPreferenceEditor.getData(Global.USER_ID)) {
                            mMemberName = "You"
                        } else {
                            val mGroupMemberDetails =
                                mAppDatabase!!.getUserDetailsDao().getUserDetails(mMemberIds[i])
                            if (mGroupMemberDetails.isNotEmpty()) {
                                var mOtherName = ""
                                for (j in mGroupMemberDetails.indices) {
                                    mGroupMemberDetails[j].name?.let {
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
                    holder.binding.textviewLastMessage.text =
                        mAppendData.plus(" ").plus("removed").plus(" ").plus(mMemberName)
                }
            } else if (listConversation[position].messageType == ChatMessageTypes.GROUPPROFILE.type) {
                if (listConversation[position].sender == SharedPreferenceEditor.getData(Global.USER_ID)) {
                    holder.binding.textviewLastMessage.text =
                        context.resources.getString(R.string.str_last_message_content)
                } else {
                    val listMemberDetail =
                        mAppDatabase!!.getUserDetailsDao()
                            .getUserDetails(listConversation[position].sender)
                    if (listMemberDetail.isNotEmpty()) {
                        holder.binding.textviewLastMessage.text =
                            listMemberDetail[0].name.plus(" ").plus("changed this group's icon")
                    }
                }
            } else if (listConversation[position].messageType == ChatMessageTypes.GROUPINFO.type ||
                listConversation[position].messageType == ChatMessageTypes.CREATEGROUP.type
            ) {
                if (listConversation[position].sender == SharedPreferenceEditor.getData(Global.USER_ID)) {
                    holder.binding.textviewLastMessage.text =
                        "You".plus(" ").plus(listConversation[position].lastMessage)
                } else {
                    val listMemberDetail =
                        mAppDatabase!!.getUserDetailsDao()
                            .getUserDetails(listConversation[position].sender)
                    if (listMemberDetail.isNotEmpty()) {
                        holder.binding.textviewLastMessage.text =
                            listMemberDetail[0].name.plus(" ")
                                .plus(listConversation[position].lastMessage)
                    }
                }
            } else if (listConversation[position].messageType == ChatMessageTypes.EXITMEMBER.type) {
                if (listConversation[position].sender == SharedPreferenceEditor.getData(Global.USER_ID)) {
                    holder.binding.textviewLastMessage.text = "You left"
                } else {
                    val listMemberDetail =
                        mAppDatabase!!.getUserDetailsDao()
                            .getUserDetails(listConversation[position].sender)
                    if (listMemberDetail.isNotEmpty()) {
                        holder.binding.textviewLastMessage.text =
                            listMemberDetail[0].name.plus(" ").plus("left")
                    }
                }
            } else {
                holder.binding.textviewLastMessage.text = listConversation[position].lastMessage
            }

            holder.binding.textviewTime.text = listConversation[position].time
            if (listConversation[position].messageCount != 0) {
                holder.binding.textviewMessageCount.visibility = View.VISIBLE
                holder.binding.textviewLastMessage.text =
                    listConversation[position].messageCount.toString()
            } else {
                holder.binding.textviewMessageCount.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            onItemClicked.invoke(listConversation[position])
        }

    }

    class ViewHolder(var binding: ItemConversationBinding) : RecyclerView.ViewHolder(binding.root)
}

