package com.example.tfgandroid.fragments.generatingVideoFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Image
import com.example.tfgandroid.repositories.P2PRepository

class GeneratingVideoFragmentVM(private val p2PRepository: P2PRepository): ViewModel() {

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

    fun closeGroup()
    {
        p2PRepository.closeGroup()
    }

    fun getSessionStatus(): LiveData<Int>
    {
        return p2PRepository.getSessionStatus()
    }
}