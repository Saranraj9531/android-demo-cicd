package com.sparkout.chat.roomdb.interfaces

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sparkout.chat.ui.chat.model.AudioStatus
import com.sparkout.chat.ui.groupchat.model.MessageStatusModel

@Dao
interface AudioStatusDao {
    @RawQuery
    fun insertDataRawFormat(query: SimpleSQLiteQuery): List<AudioStatus>

    @Insert
    fun insert(audioStatus: AudioStatus)

    @Update
    fun update(audioStatus: AudioStatus)

    @Delete
    fun delete(audioStatus: AudioStatus)

    @Delete
    fun deleteList(listAudioStatus: List<AudioStatus>)

    @Query("DELETE FROM AudioStatus")
    fun deleteAll()

    @Query("SELECT * from AudioStatus WHERE messageId =:mMessageId AND userId=:mUserId")
    fun getAudioMessage(mMessageId: String,mUserId: String): List<AudioStatus>

    @Query("UPDATE AudioStatus SET checkPlayed =1 WHERE messageId =:mMessageId AND userId=:mUserId")
    fun updateAudioStatus(mMessageId: String,mUserId: String): Unit

    @Query("SELECT * from AudioStatus WHERE messageId =:mMessageId AND userId =:mUserId AND checkPlayed =1")
    fun checkAlreadyExists(mMessageId: String, mUserId: String): List<AudioStatus>
}