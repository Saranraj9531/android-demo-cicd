package com.sparkout.chat.ui.groupchat.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor

@Entity(tableName = "GroupMember")
class GroupMemberModel {
    @PrimaryKey
    var objectId: String = ""
    var memberId: String = ""
    var groupId: String = ""
    var checkRemoved: Boolean = false
    var checkAdmin: Boolean = false
    var userId: String = SharedPreferenceEditor.getData(Global.USER_ID)
}