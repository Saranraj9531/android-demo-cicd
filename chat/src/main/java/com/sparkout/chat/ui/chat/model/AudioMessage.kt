package com.sparkout.chat.ui.chat.model

import com.google.gson.annotations.SerializedName

class AudioMessage {
    var sender: String = ""
    var receiver: String = ""
    var status: String = ""

    @SerializedName("message_id")
    var messageId: String = ""
}