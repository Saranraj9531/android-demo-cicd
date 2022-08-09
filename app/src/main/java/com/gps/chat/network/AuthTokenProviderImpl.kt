package com.gps.chat.network

import com.gps.chat.utils.SharedPreferencesEditor
import javax.inject.Inject
import javax.inject.Singleton

/**
 *Created by Nivetha S on 02-02-2022.
 */
@Singleton
class AuthTokenProviderImpl @Inject constructor() : AuthProvider {

    override fun token(): String {
        return SharedPreferencesEditor.getAccessToken()
    }

    override fun persistToken(token: String?) {
        SharedPreferencesEditor.storeAccessToken(token())
    }


}