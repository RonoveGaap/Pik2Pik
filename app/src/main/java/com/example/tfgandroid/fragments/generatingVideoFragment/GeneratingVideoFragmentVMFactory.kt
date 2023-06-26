package com.example.tfgandroid.fragments.generatingVideoFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tfgandroid.repositories.P2PRepository

class GeneratingVideoFragmentVMFactory(
    private val p2PRepository: P2PRepository
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeneratingVideoFragmentVM(p2PRepository) as T
    }

}