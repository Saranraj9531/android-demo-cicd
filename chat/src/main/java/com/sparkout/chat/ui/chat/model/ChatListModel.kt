package com.sparkout.chat.ui.chat.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Created by krish on 31-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
@Entity(tableName = "ChatLists")
class ChatListModel {
    @PrimaryKey
    var objectId: String = ""
    var chatType: String = ""
    var userId: String = ""
    var sender: String = ""
    var receiverId: String = ""
    var messageId: String = ""
    var lastMessage: String = ""
    var messageType: String = ""
    var messageCount: Int = 0
    var name: String = ""
    var time: String = ""
    var profilePicture: String = ""
}