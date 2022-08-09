package com.gps.chat.ui.settings.model.request

import com.google.gson.annotations.SerializedName

class UpdateProfileModel {
    @SerializedName("full_name")
    var name: String = ""

    @SerializedName("profile_picture")
    var profilePicture: String? = null
}