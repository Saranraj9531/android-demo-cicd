package com.gps.chat.ui.login.model.request

import com.google.gson.annotations.SerializedName

class LoginRequestModel {
    var username: String = ""
    var password: String = ""

    @SerializedName("device_type")
    var deviceType: String = ""

    @SerializedName("device_token")
    var deviceToken: String = ""

    @SerializedName("device_id")
    var deviceId: String = ""

    @SerializedName("device_model")
    var deviceModel: String = ""
}