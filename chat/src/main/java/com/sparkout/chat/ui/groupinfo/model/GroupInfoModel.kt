package com.sparkout.chat.ui.groupinfo.model

import com.google.gson.annotations.SerializedName

class GroupInfoModel {
    @SerializedName("group_description")
    var groupDescription: String? = null

    @SerializedName("group_id")
    var groupId: String? = null

    @SerializedName("group_image")
    var groupImage: String? = null

    @SerializedName("group_title")
    var groupTitle: String? = null
    lateinit var receivers: ArrayList<String>
    var sender: String = ""
    //    lateinit var message: ChatModel
}