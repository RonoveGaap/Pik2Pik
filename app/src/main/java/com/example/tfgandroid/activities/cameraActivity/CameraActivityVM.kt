package com.example.tfgandroid.activities.cameraActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Image
import com.example.tfgandroid.repositories.P2PRepository

class CameraActivityVM(private val p2PRepository: P2PRepository): ViewModel() {

    fun getLastGroup(): LiveData<Group> {
        return p2PRepository.getLastGroup()
    }

    fun saveImage(image: android.media.Image, rotation: Int)
    {
        p2PRepository.saveImage(image, rotation)
    }

    fun getAllImagesFromGroup(): LiveData<List<Image>>
    {
        return p2PRepository.getAllImagesFromGroup()
    }

    fun getImagedAddedCallback(): LiveData<Int>
    {
        return p2PRepository.getImagedAddedCallback()
    }

    fun abandonGroup()
    {
        p2PRepository.sendIntentionToLeave()
    }

    fun getSessionStatus(): LiveData<Int> {
        return p2PRepository.getSessionStatus()
    }
}