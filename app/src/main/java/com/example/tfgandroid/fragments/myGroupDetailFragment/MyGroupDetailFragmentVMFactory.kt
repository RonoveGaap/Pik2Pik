package com.example.tfgandroid.fragments.myGroupDetailFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tfgandroid.repositories.P2PRepository

class MyGroupDetailFragmentVMFactory(
    private val p2PRepository: P2PRepository
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyGroupDetailFragmentVM(p2PRepository) as T
    }

}