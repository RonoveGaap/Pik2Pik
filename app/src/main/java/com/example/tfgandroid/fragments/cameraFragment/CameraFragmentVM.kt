package com.example.tfgandroid.fragments.cameraFragment

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.tfgandroid.AppContainer
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Image
import com.example.tfgandroid.fragments.groupDetails.GroupDetailsFragmentVM
import com.example.tfgandroid.repositories.P2PRepository

class CameraFragmentVM(private val p2PRepository: P2PRepository): ViewModel() {

    fun getLastGroup(): LiveData<Group> {
        return p2PRepository.getLastGroup()
    }

    fun getLastImageFromGroup(): LiveData<Image>
    {
        return p2PRepository.getLastImageFromGroup()
    }

}