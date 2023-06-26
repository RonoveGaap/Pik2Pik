package com.example.tfgandroid.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.GroupMember

@Dao
interface GroupMemberDao {

    @Query("SELECT * FROM `group_member` WHERE group_id = :groupId")
    fun getAllGroupMembersFromGroup(groupId: Long): List<GroupMember>

    @Query("SELECT * FROM `group_member` WHERE group_id = :groupId")
    fun getLiveAllGroupMembersFromGroup(groupId: Long): LiveData<List<GroupMember>>

//    @Query("SELECT * FROM `group`")
//    fun getAllGroupsLive(): LiveData<List<Group>>

    @Insert
    fun insertGroupMember(groupMember: GroupMember): Long

    @Delete
    fun delete(groupMember: GroupMember)

}