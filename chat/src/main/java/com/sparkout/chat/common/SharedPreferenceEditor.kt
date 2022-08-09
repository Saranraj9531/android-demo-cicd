package com.sparkout.chat.common

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sparkout.chat.common.Global.AUTHORIZATION
import com.sparkout.chat.common.Global.DATE_OF_BIRTH
import com.sparkout.chat.common.Global.GENDER
import com.sparkout.chat.common.Global.IS_LOGIN
import com.sparkout.chat.common.Global.MOBILE_NUMBER
import com.sparkout.chat.common.Global.PROFILE_IMAGE
import com.sparkout.chat.common.Global.TOKEN
import com.sparkout.chat.common.Global.TUID
import com.sparkout.chat.common.Global.USERNAME
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.model.UserData

object SharedPreferenceEditor {

    private val masterKey = MasterKey.Builder(ChatApp.mInstance)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private fun getEncryptedSharedPreferences(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            ChatApp.mInstance,
            "secret_shared_prefs_chat",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveData(key: String, value: String) {
        getEncryptedSharedPreferences().edit()
            .putString(key, value)
            .apply()
    }

    fun saveDataBoolean(key: String, value: Boolean) {
        getEncryptedSharedPreferences().edit()
            .putBoolean(key, value)
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

    fun getBoolean(key: String): Boolean {
        return getEncryptedSharedPreferences().getBoolean(
            key, false
        )
    }

    fun isFirstRunMenu(forWhat: String): Boolean {
        return if (getBoolean(forWhat)) {
            saveDataBoolean(forWhat, false)
            true
        } else {
            false
        }
    }

    fun isFirstRun(forWhat: String): Boolean {
        return if (getBoolean(forWhat)) {
            saveDataBoolean(forWhat, false)
            true
        } else {
            false
        }
    }


    fun removeData(key: String) {
        SharedPreferenceEditor.removeData(key)
    }

    fun clearAllData() {
        SharedPreferenceEditor.clearAllData()
    }

    fun saveUserData(body: UserData) {
        with(body) {
            getEncryptedSharedPreferences().edit()
                .putString(USERNAME, body.username)
                .putString(GENDER, body.gender)
                .putString(DATE_OF_BIRTH, body.dateOfBirth)
                .putString(PROFILE_IMAGE, body.profilePicture)
                .putString(MOBILE_NUMBER, body.mobileNo)
                .putInt(TOKEN, body.token.toInt())
                .putInt(TUID, body.tUid.toInt())
                .putString(USER_ID, body.id)
                .apply()
        }

    }

    fun login(value: Boolean) {
        saveDataBoolean(IS_LOGIN, true)
    }

    fun storeAccessToken(token: String) {
        saveData(AUTHORIZATION, token)
    }

    fun getAccessToken(): String {
        return getData(AUTHORIZATION)
    }

}