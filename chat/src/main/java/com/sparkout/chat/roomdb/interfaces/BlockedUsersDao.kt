package com.sparkout.chat.roomdb.interfaces

import androidx.room.*
import com.sparkout.chat.common.model.BlockedUsersModel
import com.sparkout.chat.ui.chat.model.AudioStatus

/**
 *Created by Krishnaprakash K on 03-04-2021.
 *Copyright (c) 2021 Sparkout Tech Solutions LLP. All rights reserved.
 */
@Dao
interface BlockedUsersDao {
    @Insert
    fun insert(blockedUsers: BlockedUsersModel)

    @Update
    fun update(blockedUsers: BlockedUsersModel)

    @Delete
    fun delete(blockedUsers: BlockedUsersModel)

    @Delete
    fun deleteList(listBlockedUsers: List<BlockedUsersModel>)

    @Query("DELETE FROM BlockedUsers")
    fun deleteAll()

    @Query("SELECT * from BlockedUsers WHERE friendId =:mFriendId AND userId=:mUserId")
    fun getBlockedUsers(mFriendId: String, mUserId: String): List<BlockedUsersModel>

    @Query("SELECT * from BlockedUsers WHERE friendId =:mFriendId AND userId=:mUserId AND checkBlockedByUser = 1")
    fun checkAlreadyBlockedByUser(mFriendId: String, mUserId: String): List<BlockedUsersModel>

    @Query("SELECT * from BlockedUsers WHERE friendId =:mFriendId AND userId=:mUserId AND checkBlockedByYou = 1")
    fun checkAlreadyBlockedByYou(mFriendId: String, mUserId: String): List<BlockedUsersModel>
}