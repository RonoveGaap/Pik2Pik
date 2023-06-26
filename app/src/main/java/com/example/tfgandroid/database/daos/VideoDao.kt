package com.example.tfgandroid.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Image
import com.example.tfgandroid.database.models.Video

@Dao
interface VideoDao {

    @Query("SELECT * FROM `video` WHERE group_id = :groupId")
    fun getVideoLiveData(groupId: Long): LiveData<Video>

    @Query("SELECT * FROM `video` WHERE group_id = :groupId")
    fun getVideo(groupId: Long): Video

    @Insert
    fun insertVideo(video: Video): Long

    @Delete
    fun delete(video: Video)

}