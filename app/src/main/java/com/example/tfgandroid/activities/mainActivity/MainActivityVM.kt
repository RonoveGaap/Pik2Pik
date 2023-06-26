package com.example.tfgandroid.activities.mainActivity

import androidx.lifecycle.ViewModel
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.repositories.P2PRepository

class MainActivityVM(
    private val p2PRepository: P2PRepository
): ViewModel() {

    fun createGroup(group: Group) {
        p2PRepository.createGroup(group)
    }

}