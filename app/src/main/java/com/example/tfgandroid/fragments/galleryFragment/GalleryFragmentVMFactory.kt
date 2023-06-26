package com.example.tfgandroid.fragments.galleryFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tfgandroid.repositories.P2PRepository

class GalleryFragmentVMFactory(
    private val p2PRepository: P2PRepository
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GalleryFragmentVM(p2PRepository) as T
    }

}