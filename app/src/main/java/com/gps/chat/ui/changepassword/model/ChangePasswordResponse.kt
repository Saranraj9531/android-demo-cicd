package com.gps.chat.ui.changepassword.model

import com.gps.chat.ui.login.model.response.UserData

class ChangePasswordResponse {
    var status: Boolean = false
    var message: String = ""
    var data: UserData? = null
}