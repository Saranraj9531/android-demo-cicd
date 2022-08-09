package com.sparkout.chat.ui.groupinfo.model

import com.google.gson.annotations.SerializedName

class AddOrRemoveAdminModel {
    var admin: String? = ""

    @SerializedName("group_id")
    var groupId: String? = ""
    lateinit var receivers: ArrayList<String>
}