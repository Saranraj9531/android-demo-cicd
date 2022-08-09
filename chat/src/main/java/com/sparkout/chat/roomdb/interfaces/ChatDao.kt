package com.sparkout.chat.roomdb.interfaces

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sparkout.chat.common.model.UserDetailsModel
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.groupchat.model.MessageStatusModel

// Created by krish on 10-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
@Dao
interface ChatDao {
    @RawQuery
    fun insertDataRawFormat(query: SimpleSQLiteQuery): List<ChatModel>

    @Insert
    fun insert(chatModel: ChatModel)

    @Update
    fun update(chatModel: ChatModel)

    @Delete
    fun delete(chatModel: ChatModel)

    @Delete
    fun delete(listChatModel: List<ChatModel>)

    @Delete
    fun deleteList(listChatModel: List<ChatModel>)

    @Query("SELECT * FROM Chat WHERE (receiver =:id AND sender =:mUserId) OR (receiver =:mUserId AND sender =:id) ORDER BY chatTime ASC")
    fun getChat(id: String, mUserId: String): List<ChatModel>

    @Query("SELECT * FROM Chat WHERE messageId =:mMessageId AND userId=:mUserId")
    fun getChatData(mMessageId: String, mUserId: String): List<ChatModel>

    @Query("SELECT * FROM Chat WHERE (sender =:mReceiverId AND receiver =:mUserId AND messageStatus != 3)")
    fun getMessageCount(mUserId: String, mReceiverId: String): List<ChatModel>

    @Query("SELECT * FROM Chat WHERE (sender =:mUserId AND receiver =:mReceiverId AND messageStatus < 3)")
    fun getMessageRead(mUserId: String, mReceiverId: String): List<ChatModel>

    @Query("SELECT * FROM Chat WHERE (sender =:mUserId AND receiver =:mReceiverId AND messageType = :mMessageType) OR (sender =:mReceiverId AND receiver =:mUserId AND messageType = :mMessageType)")
    fun getMessageMedia(mUserId: String, mReceiverId: String, mMessageType: String): List<ChatModel>

    @Query("SELECT * FROM Chat WHERE (sender =:mUserId AND messageStatus == 0 AND chatType == 1 AND messageStatus != 101 AND messageStatus != 102)")
    fun getOfflineMessages(mUserId: String): List<ChatModel>

    @Query("SELECT * FROM Chat WHERE (receiver =:id AND sender =:mUserId AND messageType =:mMessageType) OR (receiver =:mUserId AND sender =:id AND messageType =:mMessageType)")
    fun getChatType(id: String, mUserId: String, mMessageType: String): List<ChatModel>

    @Query("SELECT * FROM Chat WHERE messageType =:mMessageType AND sender =:mUserId")
    fun getUnreadList(mMessageType: String, mUserId: String): List<ChatModel>

    @Query("UPDATE Chat SET messageStatus =:mMessageStatus WHERE messageType IN (:mMediaList) AND receiver =:mReceiverId AND sender =:mUserId")
    fun updateRetryStatus(mMessageStatus: String,
                          mMediaList: ArrayList<String>,
                          mReceiverId: String,
                          mUserId: String): Unit

    @Query("DELETE FROM Chat WHERE messageType =:mMessageType AND receiver =:mReceiverId AND sender =:mUserId")
    fun deleteUnreadMessage(mMessageType: String, mReceiverId: String, mUserId: String)

    @Query("SELECT * FROM Chat WHERE messageStatus =:mMessageStatus AND messageType IN (:mMediaList) AND sender =:mUserId")
    fun fetchUnsentMediaMessages(mMessageStatus: String,
                                 mMediaList: ArrayList<String>,
                                 mUserId: String): List<ChatModel>

    @Query("DELETE FROM Chat")
    fun deleteAll()

    /**
     * Group related queries
     * */
    @Query("SELECT * FROM Chat WHERE (receiver =:id AND sender =:mUserId) OR (receiver =:id AND sender !=:mUserId) ORDER BY chatTime ASC")
    fun getGroupChat(id: String, mUserId: String): List<ChatModel>

    @Query("SELECT * FROM Chat WHERE messageId =:mMessageId AND userId=:mUserId")
    fun getGroupChatData(mMessageId: String, mUserId: String): List<ChatModel>

    @Query("SELECT * FROM Chat WHERE (sender !=:mUserId AND receiver =:mReceiverId AND messageStatus != 3 AND NOT messageType IN (:mListInfoMessageTypes))")
    fun getGroupMessageCount(mUserId: String,
                             mReceiverId: String,
                             mListInfoMessageTypes: ArrayList<String>): List<ChatModel>

    @Query("UPDATE Chat SET messageStatus =3 WHERE receiver =:mGroupId AND sender !=:mUserId")
    fun updateGroupChatReadStatus(mGroupId: String, mUserId: String): Unit

    @Query("UPDATE Chat SET messageType =17 WHERE messageId =:mMessageId")
    fun updateMessageForDeleteForEveryone(mMessageId: String): Unit

    //    fun updateMessageForDeleteForEveryone(mMessageId: String, mUserId: String): Unit
    @Query("SELECT * FROM Chat WHERE messageType =:mMessageType AND receiver =:mReceiverId AND userId=:mUserId")
    fun checkMessageType(mMessageType: String,
                         mReceiverId: String, mUserId: String): List<ChatModel>

    @Query("SELECT * FROM Chat WHERE (sender =:mUserId AND receiver =:mReceiverId AND messageType = :mMessageType) OR (sender !=:mUserId AND receiver =:mReceiverId AND messageType = :mMessageType)")
    fun getGroupMessageMedia(mUserId: String,
                             mReceiverId: String,
                             mMessageType: String): List<ChatModel>

    @Query("UPDATE Chat SET userId =:mUserId WHERE messageId =:mMessageId")
    fun updateUserId(mUserId: String,
                     mMessageId: String)

    @Query("SELECT * FROM Chat")
    fun getChatDetails(): List<ChatModel>
}