package com.sparkout.chat.ui.chat.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.SharedPreferenceEditor

@Entity(tableName = "AudioStatus")
class AudioStatus {
    @PrimaryKey
    var messageId: String = ""
    var userId: String = ""
    var checkPlayed: Boolean = false
    lateinit var receivers: ArrayList<String>
}