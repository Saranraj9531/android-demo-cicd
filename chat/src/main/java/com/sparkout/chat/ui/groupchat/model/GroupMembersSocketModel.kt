package com.sparkout.chat.ui.groupchat.model

import com.google.gson.annotations.SerializedName

class GroupMembersSocketModel {
    var sender: String = ""

    @SerializedName("group_id")
    var groupId: String = ""
}