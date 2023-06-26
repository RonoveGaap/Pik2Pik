package com.example.tfgandroid.fragments.myGroupsFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.repositories.P2PRepository

class MyGroupsFragmentVM(private val p2PRepository: P2PRepository): ViewModel() {

    fun getAllGroups(): LiveData<List<Group>>
    {
        return p2PRepository.getAllGroups()
    }

    fun deleteGroup(groupId: Long)
    {
        return p2PRepository.deleteGroupFromDatabase(groupId)
    }
}