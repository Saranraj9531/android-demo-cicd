package com.sparkout.chat.roomdb.interfaces

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sparkout.chat.ui.chat.model.ChatListModel

// Created by krish on 10-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
@Dao
interface ChatListDao {
    @RawQuery
    fun insertDataRawFormat(query: SimpleSQLiteQuery): List<ChatListModel>

    @Insert
    fun insert(chatListModel: ChatListModel)

    @Update
    fun update(chatListModel: ChatListModel)

    @Delete
    fun delete(chatListModel: ChatListModel)

    @Delete
    fun delete(chatListModel: List<ChatListModel>)

    @Query("SELECT * FROM ChatLists")
    fun getChatList(): List<ChatListModel>

    @Query("SELECT * FROM ChatLists WHERE userId =:mUserId ORDER BY time DESC")
    fun getChatList(mUserId: String): List<ChatListModel>

    @Query("SELECT * FROM ChatLists WHERE (receiverId =:mReceiverId AND userId =:mUserId)")
    fun getChatList(mReceiverId: String, mUserId: String): List<ChatListModel>

    @Query("UPDATE ChatLists SET messageType =17 WHERE messageId =:mMessageId AND userId =:mUserId")
    fun updateMessageForDeleteForEveryone(mMessageId: String, mUserId: String): Unit

    @Query("SELECT * FROM ChatLists WHERE userId =:mUserId AND receiverId =:mReceiverId AND messageId =:mMessageId")
    fun isMessageExists(mUserId: String,
                        mReceiverId: String,
                        mMessageId: String): List<ChatListModel>

    @Query("DELETE FROM ChatLists")
    fun deleteAll()
}