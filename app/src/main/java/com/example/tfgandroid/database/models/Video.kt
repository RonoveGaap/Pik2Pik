package com.example.tfgandroid.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@kotlinx.serialization.Serializable
@Entity(tableName = "video")
data class Video(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "group_id") val groupId: Long,
) {
    constructor(groupId: Long): this(null, groupId)
}
