package com.gps.chat.ui.forgotpassword.model.response

import com.gps.chat.ui.login.model.response.UserData


class ForgotPasswordResponse {
    var status: Boolean = false
    var message: String = ""
    lateinit var data: UserData
}