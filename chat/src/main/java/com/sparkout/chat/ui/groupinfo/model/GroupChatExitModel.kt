package com.sparkout.chat.ui.groupinfo.model

import com.google.gson.annotations.SerializedName

class GroupChatExitModel {
    var sender: String = ""
    lateinit var receivers: ArrayList<String>

    @SerializedName("group_id")
    var groupId: String = ""
}