package com.sparkout.chat.ui.groupchat.model

import com.google.gson.annotations.SerializedName

class GroupChatDeliveredModel {
    var sender: String = ""
    lateinit var receivers: ArrayList<String>

    @SerializedName("message_id")
    var messageId: String = ""

    @SerializedName("group_id")
    var groupId: String = ""
}