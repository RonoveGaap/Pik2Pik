package com.example.tfgandroid.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guest")
data class Guest(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "endpoint_id") val endpointId: String,
    @ColumnInfo(name = "group_id") val groupId: Long,
    @ColumnInfo(name = "connected_at") val connectedAt: String,
    @ColumnInfo(name = "disconnected_at") val disconnectedAt: String?
)
