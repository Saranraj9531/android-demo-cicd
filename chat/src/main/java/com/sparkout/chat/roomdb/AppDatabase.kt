package com.sparkout.chat.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sparkout.chat.common.model.BlockedUsersModel
import com.sparkout.chat.common.model.LocationModel
import com.sparkout.chat.common.model.UserDetailsModel
import com.sparkout.chat.roomdb.interfaces.*
import com.sparkout.chat.ui.chat.model.AudioStatus
import com.sparkout.chat.ui.chat.model.ChatListModel
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.groupchat.model.GroupDetailsModel
import com.sparkout.chat.ui.groupchat.model.GroupMemberModel
import com.sparkout.chat.ui.groupchat.model.MessageStatusModel

// Created by krish on 10-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
@Database(entities = [
    UserDetailsModel::class,
    ChatModel::class,
    ChatListModel::class,
    LocationModel::class,
    GroupDetailsModel::class,
    GroupMemberModel::class,
    MessageStatusModel::class,
    AudioStatus::class,
    BlockedUsersModel::class],
          version = 6,
          exportSchema = false)
@TypeConverters(ConverterModel::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getUserDetailsDao(): UserDetailsDao

    abstract fun getChatDao(): ChatDao

    abstract fun getChatListDao(): ChatListDao

    abstract fun getGroupDetailsDao(): GroupDetailsDao

    abstract fun getGroupMemberDao(): GroupMemberDao

    abstract fun getMessageStatusDao(): MessageStatusDao

    abstract fun getAudioStatusDao(): AudioStatusDao

    abstract fun getLocationDao(): LocationDao

    abstract fun getBlockedUsersDao(): BlockedUsersDao

    override fun clearAllTables() {
        this.getUserDetailsDao().deleteAll()
        this.getChatDao().deleteAll()
        this.getChatListDao().deleteAll()
        this.getGroupDetailsDao().deleteAll()
        this.getGroupMemberDao().deleteAll()
        this.getMessageStatusDao().deleteAll()
        this.getAudioStatusDao().deleteAll()
        this.getLocationDao().deleteAll()
        this.getBlockedUsersDao().deleteAll()
    }
}