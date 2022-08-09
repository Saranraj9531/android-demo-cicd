package com.sparkout.chat.roomdb.interfaces

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sparkout.chat.ui.groupchat.model.GroupMemberModel
import com.sparkout.chat.ui.groupchat.model.MessageStatusModel

@Dao
interface GroupMemberDao {
    @RawQuery
    fun insertDataRawFormat(query: SimpleSQLiteQuery): List<GroupMemberModel>

    @Insert
    fun insert(groupMemberModel: GroupMemberModel)

    @Update
    fun update(groupMemberModel: GroupMemberModel)

    @Delete
    fun delete(groupMemberModel: GroupMemberModel)

    @Delete
    fun deleteList(listGroupMember: List<GroupMemberModel>)

    @Query("SELECT * from GroupMember WHERE groupId =:mGroupId AND checkRemoved =0 AND userId=:mUserId")
    fun getGroupMembers(mGroupId: String, mUserId: String): List<GroupMemberModel>

    @Query("SELECT * from GroupMember WHERE groupId =:mGroupId AND memberId =:mMemberId AND userId=:mUserId")
    fun getGroupMemberDetails(mGroupId: String,
                              mMemberId: String,
                              mUserId: String): List<GroupMemberModel>

    @Query("UPDATE GroupMember SET checkRemoved =1, checkAdmin =0 WHERE groupId =:mGroupId AND memberId =:mMemberId")
    fun removeMember(mGroupId: String, mMemberId: String): Unit

    @Query("UPDATE GroupMember SET checkAdmin =:mBoolean WHERE groupId =:mGroupId AND memberId =:mMemberId")
    fun addOrRemoveAdmin(mGroupId: String, mMemberId: String, mBoolean: Int): Unit

    @Query("SELECT * from GroupMember WHERE groupId =:mGroupId AND memberId =:mUserId AND checkAdmin =1")
    fun getCheckAdmin(mGroupId: String, mUserId: String): List<GroupMemberModel>

    @Query("SELECT * from GroupMember WHERE groupId =:mGroupId AND checkRemoved =0 AND checkAdmin =1")
    fun fetchAdminList(mGroupId: String): List<GroupMemberModel>

    @Query("SELECT * from GroupMember WHERE groupId =:mGroupId AND memberId !=:mUserId AND checkRemoved =0")
    fun fetchMembersExceptGroupList(mGroupId: String, mUserId: String): List<GroupMemberModel>

    @Query("DELETE from GroupMember")
    fun deleteAll()

}