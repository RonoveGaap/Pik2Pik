package com.example.tfgandroid.database.models

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

@kotlinx.serialization.Serializable
@Entity(tableName = "group")
data class Group(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "closed_at") val closedAt: String?,
    @ColumnInfo(name = "group_common_prefix") val groupCommonPrefix: String,
) {
    constructor(name: String, createdAt: String, groupCommonPrefix: String): this(null, name, createdAt, null, groupCommonPrefix)

    fun getDirectory(context: Context): File
    {
        return File(context.filesDir, "group_${id!!}")
    }
}
