package com.gps.chat.ui.welcome.model.response

class CheckUserNameResponse {
    var status: Boolean = false
    var message: String = ""
    var data: AvailableStatus? = null

    inner class AvailableStatus {
        var available: Int = 0
    }

}


