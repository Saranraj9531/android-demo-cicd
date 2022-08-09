package com.sparkout.chat.ui.groupchat.model

import com.google.gson.annotations.SerializedName

class MembersModel {
    @SerializedName("_id")
    var id: String = ""

    @SerializedName("check_admin")
    var checkAdmin: Boolean? = null
}