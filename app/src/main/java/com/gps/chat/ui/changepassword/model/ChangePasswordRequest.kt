package com.gps.chat.ui.changepassword.model

import com.google.gson.annotations.SerializedName

class ChangePasswordRequest {
    var id: String = ""

    @SerializedName("old_password")
    var oldPassword: String = ""

    @SerializedName("new_password")
    var newPassword: String = ""
}