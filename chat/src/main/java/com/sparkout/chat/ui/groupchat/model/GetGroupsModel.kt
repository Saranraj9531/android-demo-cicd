package com.sparkout.chat.ui.groupchat.model

import com.google.gson.annotations.SerializedName

class GetGroupsModel {
    @SerializedName("_id")
    var id: String = ""

    @SerializedName("check_admin")
    var checkAdmin: Boolean = false

    @SerializedName("group_id")
    lateinit var group: GroupDetailsModel

    @SerializedName("member_id")
    var memberId: String = ""
}