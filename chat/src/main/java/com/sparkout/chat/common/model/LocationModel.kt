package com.sparkout.chat.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor

/**
 *Created by Nivetha S on 12-03-2021.
 */
@Entity(tableName = "Location")
class LocationModel {
    @PrimaryKey
    var messageId: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var userId: String = SharedPreferenceEditor.getData(USER_ID)

}