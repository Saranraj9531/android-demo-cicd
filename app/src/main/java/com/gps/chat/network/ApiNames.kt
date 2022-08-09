package com.gps.chat.network

/**
 *Created by Nivetha S on 11-11-2021.
 */
object ApiNames {

    object GET {
        const val LOGIN = "user/login"
    }

    object POST {
        const val CHECK_USERNAME: String = "user/check-username"
        const val USER: String = "user"
        const val USER_LOGIN: String = "user/login"
        const val USER_LOGIN_NEW_SESSION: String = "user/login/new-session"
        const val ACKNOWLEDGE_USER: String = "user/acknowledge"
        const val FORGET_PASSWORD: String = "user/forget-passowrd"
        const val CHANGE_PASSWORD: String = "user/update-password"
        const val UPLOAD: String = "upload"
        const val SEARCH_USERS: String = "user/search-user"
        const val ADS: String = "ad/home"
        const val USER_LOGOUT: String = "user/logout"
    }
}