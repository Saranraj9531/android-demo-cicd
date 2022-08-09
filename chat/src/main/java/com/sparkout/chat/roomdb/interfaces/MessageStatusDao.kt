package com.sparkout.chat.roomdb.interfaces

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sparkout.chat.ui.chat.model.ChatListModel
import com.sparkout.chat.ui.groupchat.model.MessageStatusModel

@Dao
interface MessageStatusDao {
    @RawQuery
    fun insertDataRawFormat(query: SimpleSQLiteQuery): List<MessageStatusModel>

    @Insert
    fun insert(messageStatusModel: MessageStatusModel)

    @Update
    fun update(messageStatusModel: MessageStatusModel)

    @Delete
    fun delete(messageStatusModel: MessageStatusModel)

    @Delete
    fun deleteList(listChatModel: List<MessageStatusModel>)

    @Query("DELETE FROM MessageStatus")
    fun deleteAll()

    @Query("SELECT * FROM MessageStatus WHERE groupId =:mGroupId AND messageId =:mMessageId AND userId=:mUserId")
    fun messageStatusList(mGroupId: String,
                          mMessageId: String,
                          mUserId: String): List<MessageStatusModel>

    @Query("SELECT * FROM MessageStatus WHERE messageId =:mMessageId AND userId=:mUserId")
    fun getMessageList(mMessageId: String, mUserId: String): List<MessageStatusModel>
}