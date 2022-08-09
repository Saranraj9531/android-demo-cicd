package com.sparkout.chat.roomdb.interfaces

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sparkout.chat.common.model.UserDetailsModel

// Created by krish on 10-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
@Dao
interface UserDetailsDao {
    @RawQuery
    fun insertDataRawFormat(query: SimpleSQLiteQuery): List<UserDetailsModel>

    @Insert
    fun insert(userDetailsModel: UserDetailsModel)

    @Update
    fun update(userDetailsModel: UserDetailsModel)

    @Delete
    fun delete(userDetailsModel: UserDetailsModel)

    @Query("SELECT * FROM User WHERE id =:mUserId")
    fun getUsersList(mUserId: String): List<UserDetailsModel>

    @Query("SELECT * FROM User WHERE id !=:mUserId")
    fun getUserDetailsList(mUserId: String): List<UserDetailsModel>

    @Query("SELECT * FROM User WHERE id =:id")
    fun getUserDetails(id: String): List<UserDetailsModel>

    @Query("DELETE FROM User")
    fun deleteAll()

    @Query("UPDATE User SET id =:mUserId WHERE id =:mMessageId")
    fun updateUserId(mUserId: String,
                     mMessageId: String)

    @Query("SELECT * FROM User")
    fun getUsersListDetails(): List<UserDetailsModel>
}