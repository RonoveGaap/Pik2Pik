package com.example.tfgandroid.fragments.galleryFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Image
import com.example.tfgandroid.repositories.P2PRepository

class GalleryFragmentVM(private val p2PRepository: P2PRepository): ViewModel() {

    fun getLastGroup(): LiveData<Group> {
        return p2PRepository.getLastGroup()
    }

    fun getAllImagesFromGroup(): LiveData<List<Image>>
    {
        return p2PRepository.getAllImagesFromGroup()
    }

    fun saveVideo()
    {
        return p2PRepository.saveVideo()
    }
}