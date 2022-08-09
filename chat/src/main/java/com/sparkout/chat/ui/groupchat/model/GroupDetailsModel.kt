package com.sparkout.chat.ui.groupchat.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor

@Entity(tableName = "GroupDetails")
class GroupDetailsModel {
    @PrimaryKey
    @SerializedName("group_id")
    var groupId: String = ""

    @SerializedName("group_title")
    var groupTitle: String = ""

    @SerializedName("group_image")
    var groupImage: String = ""

    @SerializedName("group_description")
    var groupDescription: String = ""

    @SerializedName("created_by")
    var createdBy: String = ""

    @SerializedName("created_date")
    var createdDate: String? = null

    @SerializedName("check_exit")
    var checkExit: Boolean = false
    var userId: String = SharedPreferenceEditor.getData(Global.USER_ID)
}