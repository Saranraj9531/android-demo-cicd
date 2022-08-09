package com.gps.chat.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.gps.chat.utils.CONSTANTS.DEVICE_TOKEN
import com.gps.chat.utils.SharedPreferencesEditor

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        SharedPreferencesEditor.storeValue(DEVICE_TOKEN, token)
    }

    companion object {
        fun getDeviceToken(): String {
            return SharedPreferencesEditor.getData(DEVICE_TOKEN)
        }
    }

}