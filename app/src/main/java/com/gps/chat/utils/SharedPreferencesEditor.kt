package com.gps.chat.utils

import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.gps.chat.ui.login.model.response.UserData
import com.gps.chat.utils.CONSTANTS.ACKNOWLEDGED_COUNT
import com.gps.chat.utils.CONSTANTS.ACKNOWLEDGED_USERS
import com.gps.chat.utils.CONSTANTS.AUTHORIZATION
import com.gps.chat.utils.CONSTANTS.DATE_OF_BIRTH
import com.gps.chat.utils.CONSTANTS.DEVICE_TOKEN
import com.gps.chat.utils.CONSTANTS.DEVICE_TYPE
import com.gps.chat.utils.CONSTANTS.FULL_NAME
import com.gps.chat.utils.CONSTANTS.GENDER
import com.gps.chat.utils.CONSTANTS.IS_LOGIN
import com.gps.chat.utils.CONSTANTS.PASSPORT_IMAGE
import com.gps.chat.utils.CONSTANTS.PASSPORT_NUMBER
import com.gps.chat.utils.CONSTANTS.PASSWORD
import com.gps.chat.utils.CONSTANTS.PROFILE_IMAGE
import com.gps.chat.utils.CONSTANTS.USERNAME
import com.gps.chat.utils.CONSTANTS.USER_ACCESS_TOKEN
import com.gps.chat.utils.CONSTANTS.USER_ID
import com.gps.chat.utils.CONSTANTS.USER_STATUS
import com.gps.chat.utils.CONSTANTS.USER_TYPE

object SharedPreferencesEditor {

    private val masterKey = MasterKey.Builder(App.mInstance)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private fun getEncryptedSharedPreferences(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            App.mInstance,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun storeAccessToken(token: String) {
        storeValue(USER_ACCESS_TOKEN, "Bearer ".plus(token))
    }

    fun getAccessToken(): String {
        return getData(USER_ACCESS_TOKEN)
    }

    fun storeValue(key: String, value: String) {
        getEncryptedSharedPreferences().edit()
            .putString(key, value)
            .apply()
    }

    fun storeValueBoolean(key: String, value: Boolean) {
        getEncryptedSharedPreferences().edit()
            .putBoolean(key, value)
            .apply()
    }

    fun storeValue(key: String, value: Int) {
        getEncryptedSharedPreferences().edit()
            .putInt(key, value)
            .apply()
    }

    fun getData(key: String): String {
        return getEncryptedSharedPreferences().getString(
            key, ""
        ).toString()
    }

    fun getDataInt(key: String): Int {
        return getEncryptedSharedPreferences().getInt(
            key, 0
        )
    }

    fun getDataBoolean(key: String): Boolean {
        return getEncryptedSharedPreferences().getBoolean(
            key, false
        )
    }

    fun saveUserData(userData: UserData) {
        with(userData) {
            Log.e("Nive ", "saveUserData: ${id}")
            getEncryptedSharedPreferences().edit()
                .putString(USERNAME, username)
                .putString(GENDER, gender)
                .putString(DATE_OF_BIRTH, dateOfBirth)
                .putString(FULL_NAME, name)
                .putString(PASSPORT_NUMBER, passportNumber)
                .putString(PASSPORT_IMAGE, passportImage)
                .putString(PROFILE_IMAGE, profilePicture)
                .putString(PASSWORD, password)
                .putString(DEVICE_TOKEN, deviceToken)
                .putString(DEVICE_TYPE, deviceType)
                .putInt(USER_STATUS, userStatus)
                .putInt(USER_TYPE, userType)
                .putInt(ACKNOWLEDGED_COUNT, acknowledgedCount)
                .putString(ACKNOWLEDGED_USERS, acknowledgedUsers.toString())
                .putBoolean(IS_LOGIN, true)
                .putString(USER_ID, id)
                .putString(AUTHORIZATION, session?.authorization)
                .apply()
        }
        Log.e("Nive ", "saveUserData:UserId ${getData(USER_ID)}")
        Log.e("Nive ", "saveUserData:Authorization ${getData(AUTHORIZATION)}")
    }

    fun clearAllData() {
        getEncryptedSharedPreferences().edit().clear().apply()
    }


}