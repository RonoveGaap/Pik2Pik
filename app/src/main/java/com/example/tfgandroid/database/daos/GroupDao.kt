package com.example.tfgandroid.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.tfgandroid.database.models.Group

@Dao
interface GroupDao {

    @Query("SELECT * FROM `group`")
    fun getAllGroups(): List<Group>

    @Query("SELECT * FROM `group`")
    fun getAllGroupsLive(): LiveData<List<Group>>

    @Query("SELECT * FROM `group` WHERE id = :id")
    fun getGroupById(id: Long): Group

    @Query("SELECT * FROM `group` ORDER BY id DESC LIMIT 1")
    fun getLastGroup(): LiveData<Group>

    @Query("UPDATE `group` SET closed_at = :closeDate WHERE id = :id")
    fun closeGroup(id: Long, closeDate: String)

    @Query("DELETE FROM `group` WHERE id = :id")
    fun deleteGroupById(id: Long)

    @Insert
    fun insertGroup(group: Group): Long

    @Delete
    fun delete(group: Group)

}