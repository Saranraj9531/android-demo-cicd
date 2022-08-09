package com.sparkout.chat.ui.groupchat.model

import com.google.gson.annotations.SerializedName

class GroupChatReadModel {
    var sender: String = ""
    lateinit var receivers: ArrayList<String>

    @SerializedName("message_id")
    lateinit var messageId: ArrayList<String>

    @SerializedName("group_id")
    var groupId: String = ""
}