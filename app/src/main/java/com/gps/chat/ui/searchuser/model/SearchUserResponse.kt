package com.gps.chat.ui.searchuser.model

import com.sparkout.chat.common.model.UserDetailsModel


class SearchUserResponse {
    var status: Boolean = false
    var message: String = ""
    var data: ArrayList<UserDetailsModel> = ArrayList()
}