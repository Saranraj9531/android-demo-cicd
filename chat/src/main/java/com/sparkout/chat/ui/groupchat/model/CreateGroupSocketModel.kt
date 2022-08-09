package com.sparkout.chat.ui.groupchat.model

import com.google.gson.annotations.SerializedName
import com.sparkout.chat.ui.chat.model.ChatModel

class CreateGroupSocketModel {
    @SerializedName("group_id")
    var groupId: String = ""

    @SerializedName("group_title")
    var groupTitle: String = ""

    @SerializedName("group_image")
    var groupImage: String = ""

    @SerializedName("group_description")
    var groupDescription: String = ""

    @SerializedName("created_by")
    var createdBy: String = ""
    lateinit var members: ArrayList<MembersModel>
    lateinit var message: ChatModel
}