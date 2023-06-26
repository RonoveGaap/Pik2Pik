package com.example.tfgandroid.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Guest

@Dao
interface GuestDao {

    @Query("SELECT * FROM `guest`")
    fun getAllGuests(): List<Guest>

    @Query("SELECT * FROM `guest` WHERE id = :id")
    fun getGuestById(id: Int): Guest

    @Query("SELECT * FROM `guest` WHERE group_id = :groupId AND disconnected_at IS NULL")
    fun getActiveMembersFromGroup(groupId: Long): LiveData<List<Guest>>

    @Query("UPDATE `guest` SET disconnected_at = :disconnectedDate WHERE id = :id")
    fun closeGuest(id: Int, disconnectedDate: String)

    @Insert
    fun insertGuest(guest: Guest)

    @Delete
    fun delete(guest: Guest)

}