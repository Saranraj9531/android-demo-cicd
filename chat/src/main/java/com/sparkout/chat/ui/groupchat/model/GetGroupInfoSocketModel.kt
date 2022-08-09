package com.sparkout.chat.ui.groupchat.model

import com.google.gson.annotations.SerializedName

class GetGroupInfoSocketModel {
    @SerializedName("group_id")
    var groupId:String=""
    var sender:String=""
}