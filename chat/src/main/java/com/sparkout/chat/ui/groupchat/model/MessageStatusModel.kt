package com.sparkout.chat.ui.groupchat.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor

@Entity(tableName = "MessageStatus")
class MessageStatusModel {
    @PrimaryKey
    var messageId: String = ""
    var groupId: String = ""
    lateinit var seenId: ArrayList<String>
    lateinit var deliveredId: ArrayList<String>
    var userId: String = SharedPreferenceEditor.getData(USER_ID)

}