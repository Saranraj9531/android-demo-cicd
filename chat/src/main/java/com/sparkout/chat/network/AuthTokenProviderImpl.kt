package com.sparkout.chat.network

import com.sparkout.chat.common.SharedPreferenceEditor
import javax.inject.Inject
import javax.inject.Singleton

/**
 *Created by Nivetha S on 02-02-2022.
 */
@Singleton
class AuthTokenProviderImpl @Inject constructor() : AuthProvider {

    override fun token(): String {
        return SharedPreferenceEditor.getAccessToken()
    }

    override fun persistToken(token: String?) {
        SharedPreferenceEditor.storeAccessToken(token())
    }


}