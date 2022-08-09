package com.sparkout.chat.ui.chat.model

import com.google.gson.annotations.SerializedName

class ChatDeleteMessageEveryoneSocketModel {
    var receiver: String = ""
    lateinit var message: MessageModel

    inner class MessageModel {
        var sender: String = ""

        @SerializedName("message_id")
        var messageId: String = ""
        var status: String = ""
    }
}