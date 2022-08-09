package com.gps.chat.network

/**
 *Created by Nivetha S on 02-02-2022.
 */
interface AuthProvider {
    fun token(): String
    fun persistToken(token: String?)

}