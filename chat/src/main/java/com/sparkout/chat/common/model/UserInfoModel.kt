package com.sparkout.chat.common.model

import com.google.gson.annotations.SerializedName

class UserInfoModel {
    @SerializedName("_id")
    var id: String = ""

    @SerializedName("profile_picture")
    var profilePicture: String = ""
    var username: String = ""

    @SerializedName("last_seen")
    var lastSeen: String = ""
}