package com.sparkout.chat.ui.groupinfo.model

import com.google.gson.annotations.SerializedName
import com.sparkout.chat.ui.chat.model.ChatModel

class GroupRemoveMember {
    @SerializedName("group_id")
    var groupId: String = ""

    @SerializedName("removed_members")
    lateinit var removedMembers: ArrayList<String>
    lateinit var members: ArrayList<String>
    var sender: String = ""
    lateinit var message: ChatModel
}