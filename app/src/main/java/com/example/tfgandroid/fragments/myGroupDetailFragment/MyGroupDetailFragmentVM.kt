package com.example.tfgandroid.fragments.myGroupDetailFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.models.MyGroup
import com.example.tfgandroid.repositories.P2PRepository

class MyGroupDetailFragmentVM(private val p2PRepository: P2PRepository): ViewModel() {

    fun getGroupDetails(groupId: Long): MyGroup
    {
        return p2PRepository.getGroupDetails(groupId)
    }

    fun deleteGroup(groupId: Long)
    {
        return p2PRepository.deleteGroupFromDatabase(groupId)
    }
}