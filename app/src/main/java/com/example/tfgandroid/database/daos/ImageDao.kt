package com.example.tfgandroid.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Image

@Dao
interface ImageDao {

    @Query("SELECT * FROM `image` WHERE group_id = :groupId")
    fun getAllImagesLiveData(groupId: Long): LiveData<List<Image>>

    @Query("SELECT * FROM `image` WHERE group_id = :groupId")
    fun getAllImages(groupId: Long): List<Image>

    @Query("SELECT * FROM `image` WHERE group_id = :groupId ORDER BY id DESC limit 1")
    fun getLastImageLiveData(groupId: Long): LiveData<Image>

    @Query("UPDATE `image` SET is_synchronized = 1 WHERE id IN (:values)")
    fun changeSynchronizeStatus(values: List<Long>)

    @Query("SELECT * FROM `image` WHERE hash_code = :hash")
    fun getImageByHash(hash: String): List<Image>

    @Insert
    fun insertImage(image: Image): Long

    @Query("UPDATE `image` SET path = :path WHERE id = :id")
    fun updateImagePath(path: String, id: Long)

    @Delete
    fun delete(image: Image)

}