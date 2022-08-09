package com.sparkout.chat.ui.groupchat.model

import com.google.gson.annotations.SerializedName

class GetGroupMembersModel {
    @SerializedName("_id")
    var id: String = ""

    @SerializedName("check_admin")
    var checkAdmin: Boolean = false

    @SerializedName("group_id")
    lateinit var groupId: GroupDetailsModel

    @SerializedName("member_id")
    var memberId: String = ""
}