package com.sparkout.chat.ui.chat.model

import com.google.gson.annotations.SerializedName

class ChatDeletedSocketModel {
    var sender: String = ""

    @SerializedName("message_id")
    var messageId: String = ""
    var status: String = ""
}