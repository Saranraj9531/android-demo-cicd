package com.gps.chat.ui.login.model.response

import com.google.gson.annotations.SerializedName

class UserData {

    @SerializedName("user_status")
    var userStatus: Int = 0

    @SerializedName("user_type")
    var userType: Int = 0

    @SerializedName("acknowledged_count")
    var acknowledgedCount: Int = 0
    var status: Int = 0

    @SerializedName("_id")
    var id: String = ""
    var name: String = ""
    var username: String = ""

    @SerializedName("profile_picture")
    var profilePicture: String = ""

    @SerializedName("device_type")
    var deviceType: String = ""

    @SerializedName("device_token")
    var deviceToken: String = ""
    var gender: String = ""

    @SerializedName("passport_number")
    var passportNumber: String = ""

    @SerializedName("passport_image")
    var passportImage: String = ""

    @SerializedName("date_of_birth")
    var dateOfBirth: String = ""
    var password: String = ""

    @SerializedName("acknowledged_users")
    lateinit var acknowledgedUsers: ArrayList<String>
    lateinit var contacts: ArrayList<String>
    var session: SessionData? = null

    class SessionData {
        var authorization: String = ""

        @SerializedName("device_id")
        var deviceId: String = ""

        @SerializedName("device_model")
        var deviceModel: String = ""
    }
}