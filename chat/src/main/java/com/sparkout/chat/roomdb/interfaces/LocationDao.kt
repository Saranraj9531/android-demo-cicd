package com.sparkout.chat.roomdb.interfaces

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sparkout.chat.common.model.LocationModel
import com.sparkout.chat.ui.chat.model.AudioStatus
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.groupchat.model.MessageStatusModel

@Dao
interface LocationDao {
    @RawQuery
    fun insertDataRawFormat(query: SimpleSQLiteQuery): List<LocationModel>

    @Insert
    fun insert(mLocatioModel: LocationModel)

    @Update
    fun update(mLocatioModel: LocationModel)

    @Delete
    fun delete(mLocatioModel: LocationModel)

    @Delete
    fun deleteList(listAudioStatus: List<LocationModel>)

    @Query("DELETE FROM Location")
    fun deleteAll()

    @Query("SELECT * from Location WHERE messageId =:mMessageId AND userId=:mUserId")
    fun getLocationMessage(mMessageId: String, mUserId: String): List<LocationModel>

}