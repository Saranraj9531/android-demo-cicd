package com.gps.chat.ui.forgotpassword.model.request

import com.google.gson.annotations.SerializedName
class ForgotPasswordRequest {
    var username: String = ""

    @SerializedName("passport_number")
    var passportNumber: String = ""

    @SerializedName("passport_image")
    var passportImage: String = ""
}