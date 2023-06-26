package com.example.tfgandroid.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tfgandroid.database.daos.*
import com.example.tfgandroid.database.models.*

@Database(entities = [Group::class, Guest::class, GroupMember::class, Image::class, Video::class], version = 4)
abstract class AppDatabase: RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun guestDao(): GuestDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun imageDao(): ImageDao
    abstract fun videoDao(): VideoDao
}