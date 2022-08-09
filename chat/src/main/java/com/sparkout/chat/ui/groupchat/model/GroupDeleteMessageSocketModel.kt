package com.sparkout.chat.ui.groupchat.model

import com.google.gson.annotations.SerializedName

class GroupDeleteMessageSocketModel {
    lateinit var members: ArrayList<String>
    lateinit var message: MessageModel

    inner class MessageModel {
        var sender: String = ""

        @SerializedName("message_id")
        var messageId: String = ""

        @SerializedName("group_id")
        var groupId: String = ""
        var status: String = ""
    }
}