package com.sparkout.chat.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *Created by Krishnaprakash K on 03-04-2021.
 *Copyright (c) 2021 Sparkout Tech Solutions LLP. All rights reserved.
 */
@Entity(tableName = "BlockedUsers")
class BlockedUsersModel {
    var userId: String = ""
    var friendId: String = ""

    @PrimaryKey
    var objectId: String = ""
    var checkBlockedByUser: Boolean = false
    var checkBlockedByYou: Boolean = false
}