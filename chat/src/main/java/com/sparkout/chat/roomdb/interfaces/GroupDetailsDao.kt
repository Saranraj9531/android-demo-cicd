package com.sparkout.chat.roomdb.interfaces

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sparkout.chat.ui.groupchat.model.GroupDetailsModel
import com.sparkout.chat.ui.groupchat.model.MessageStatusModel

@Dao
interface GroupDetailsDao {
    @RawQuery
    fun insertDataRawFormat(query: SimpleSQLiteQuery): List<GroupDetailsModel>

    @Insert
    fun insert(groupDetailsModel: GroupDetailsModel)

    @Update
    fun update(groupDetailsModel: GroupDetailsModel)

    @Delete
    fun delete(groupDetailsModel: GroupDetailsModel)

    @Delete
    fun deleteList(listGroupDetails: List<GroupDetailsModel>)

    @Query("SELECT * FROM GroupDetails WHERE groupId =:mId AND userId=:mUserId")
    fun getGroupDetails(mId: String, mUserId: String): List<GroupDetailsModel>

    @Query("UPDATE GroupDetails SET checkExit =1 WHERE groupId =:mId AND userId=:mUserId")
    fun exitFromGroup(mId: String, mUserId: String): Unit

    @Query("DELETE from GroupDetails")
    fun deleteAll()

}