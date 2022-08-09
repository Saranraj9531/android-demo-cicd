package com.gps.chat.ui.settings.model.response

import com.gps.chat.ui.login.model.response.UserData

class UpdateProfileResponse {
    var status: Boolean = false
    var message: String = ""
    lateinit var data: UserData
}