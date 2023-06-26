package com.example.tfgandroid.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@kotlinx.serialization.Serializable
@Entity(tableName = "image")
data class Image(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "path") var path: String?,
    @ColumnInfo(name = "group_id") val groupId: Long,
    @ColumnInfo(name = "owner") val owner: String,
    @ColumnInfo(name = "is_synchronized") var isSynchronized: Int,
    @ColumnInfo(name = "is_deleted") val isDeleted: Int,
    @ColumnInfo(name = "hash_code") val hashCode: String,
) {
    constructor(name: String, path: String?, groupId: Long, owner: String, hashCode: String): this(null, name, path, groupId, owner, 0, 0, hashCode)
}
