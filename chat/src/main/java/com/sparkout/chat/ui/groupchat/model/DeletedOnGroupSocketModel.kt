package com.sparkout.chat.ui.groupchat.model

import com.google.gson.annotations.SerializedName

class DeletedOnGroupSocketModel {
    @SerializedName("message_id")
    var messageId: String = ""

    @SerializedName("group_id")
    var groupId: String = ""
    var sender: String = ""
    var status: String = ""
}