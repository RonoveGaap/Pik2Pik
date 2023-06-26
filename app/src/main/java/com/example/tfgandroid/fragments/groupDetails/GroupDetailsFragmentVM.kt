package com.example.tfgandroid.fragments.groupDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.GroupMember
import com.example.tfgandroid.database.models.Guest
import com.example.tfgandroid.repositories.P2PRepository

class GroupDetailsFragmentVM(private val p2PRepository: P2PRepository): ViewModel() {

    fun getLastGroup(): LiveData<Group> {
        return p2PRepository.getLastGroup()
    }

    fun getActiveMembersOfGroup(groupId: Long): LiveData<List<GroupMember>> {
        return p2PRepository.getActiveMembersOfGroup(groupId)
    }

    fun startSession() {
        p2PRepository.startSession()
    }

    fun getSessionStatus(): LiveData<Int> {
        return p2PRepository.getSessionStatus()
    }

    fun getMyName(): String
    {
        return p2PRepository.getMyName()
    }
}