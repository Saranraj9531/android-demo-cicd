package com.gps.chat.ui.register.model.request

import com.google.gson.annotations.SerializedName

class RegistrationRequestModel {
    @SerializedName("device_token")
    var deviceToken: String = ""

    @SerializedName("device_type")
    var deviceType: String = ""
    var name: String = ""
    var username: String = ""

    @SerializedName("profile_picture")
    var profilePicture: String = ""

    @SerializedName("passport_image")
    var passportImage: String = ""

    @SerializedName("passport_number")
    var passportNumber: String = ""

    @SerializedName("date_of_birth")
    var dateOfBirth: String = ""
    var gender: String = ""

    @SerializedName("device_id")
    var deviceId: String = ""

    @SerializedName("device_model")
    var deviceModel: String = ""
}