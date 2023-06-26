package com.example.tfgandroid.fragments.groupSearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Guest
import com.example.tfgandroid.helpers.EnumHelper
import com.example.tfgandroid.p2p.models.GroupFound
import com.example.tfgandroid.repositories.P2PRepository

class GroupSearchFragmentVM(private val p2PRepository: P2PRepository): ViewModel() {

    fun getGroups(): LiveData<List<Group>> {
        return p2PRepository.getAllGroups()
    }

    fun getFoundGroups(): LiveData<ArrayList<GroupFound>> {
        return p2PRepository.getAllFoundGroups()
    }

    fun getSessionStatus(): LiveData<Int> {
        return p2PRepository.getSessionStatus()
    }

    fun joinGroup(endpointId: String) {
        return p2PRepository.joinGroup(endpointId)
    }

    fun stopSearchingGroups()
    {
        p2PRepository.stopSearchingGroups()
    }

    fun searchGroup() {
        p2PRepository.searchGroup()
    }

    fun groupJoined(): LiveData<Int>
    {
        return p2PRepository.getGroupJoinCode()
    }

}