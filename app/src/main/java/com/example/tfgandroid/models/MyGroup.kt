package com.example.tfgandroid.models

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tfgandroid.database.models.GroupMember
import com.example.tfgandroid.database.models.Image
import com.example.tfgandroid.database.models.Video
import java.io.File


data class MyGroup(
    var id: Long?,
    val name: String,
    val createdAt: String,
    val closedAt: String?,
    val images: ArrayList<Image>,
    val groupMembers: ArrayList<GroupMember>,
    val video: Video?
) {
    constructor(name: String, createdAt: String, images: ArrayList<Image>, members: ArrayList<GroupMember>, video: Video?): this(null, name, createdAt, null, images, members, video)

}
