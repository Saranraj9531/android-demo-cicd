package com.gps.chat.ui.register.model.response

import com.gps.chat.ui.login.model.response.UserData

class RegistrationResponse {
    var status: Boolean = false
    var message: String = ""
    var data: UserData? = null
}