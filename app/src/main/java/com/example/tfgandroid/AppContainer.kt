package com.example.tfgandroid

import android.content.Context
import androidx.room.Room
import com.example.tfgandroid.activities.cameraActivity.CameraActivityVMFactory
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.activities.mainActivity.MainActivityVMFactory
import com.example.tfgandroid.database.AppDatabase
import com.example.tfgandroid.fragments.cameraFragment.CameraFragmentVMFactory
import com.example.tfgandroid.fragments.galleryFragment.GalleryFragmentVMFactory
import com.example.tfgandroid.fragments.generatingVideoFragment.GeneratingVideoFragmentVMFactory
import com.example.tfgandroid.fragments.groupDetails.GroupDetailsFragmentVMFactory
import com.example.tfgandroid.fragments.groupSearch.GroupSearchFragmentVM
import com.example.tfgandroid.fragments.groupSearch.GroupSearchFragmentVMFactory
import com.example.tfgandroid.fragments.myGroupDetailFragment.MyGroupDetailFragmentVMFactory
import com.example.tfgandroid.fragments.myGroupsFragment.MyGroupsFragmentVMFactory
import com.example.tfgandroid.repositories.P2PRepository

class AppContainer(context: Context) {

    private var applicationContext = context

    private lateinit var p2PRepository: P2PRepository
    lateinit var mainActivityVMFactory: MainActivityVMFactory
    lateinit var groupDetailsFragmentVMFactory: GroupDetailsFragmentVMFactory
    lateinit var groupSearchFragmentVMFactory: GroupSearchFragmentVMFactory
    lateinit var cameraActivityVMFactory: CameraActivityVMFactory
    lateinit var cameraFragmentVMFactory: CameraFragmentVMFactory
    lateinit var galleryFragmentVMFactory: GalleryFragmentVMFactory
    lateinit var myGroupsFragmentVMFactory: MyGroupsFragmentVMFactory
    lateinit var myGroupDetailFragmentVMFactory: MyGroupDetailFragmentVMFactory
    lateinit var generatingVideoFragmentVMFactory: GeneratingVideoFragmentVMFactory
    lateinit var database: AppDatabase

    fun startLateProperties(mainActivity: MainActivity) {
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "TFG").build()
        p2PRepository = P2PRepository(mainActivity, database.groupDao(), database.groupMemberDao(), database.imageDao(), database.videoDao())
        mainActivityVMFactory = MainActivityVMFactory(p2PRepository)
        groupDetailsFragmentVMFactory = GroupDetailsFragmentVMFactory(p2PRepository)
        groupSearchFragmentVMFactory = GroupSearchFragmentVMFactory(p2PRepository)
        cameraActivityVMFactory = CameraActivityVMFactory(p2PRepository)
        cameraFragmentVMFactory = CameraFragmentVMFactory(p2PRepository)
        galleryFragmentVMFactory = GalleryFragmentVMFactory(p2PRepository)
        myGroupsFragmentVMFactory = MyGroupsFragmentVMFactory(p2PRepository)
        myGroupDetailFragmentVMFactory = MyGroupDetailFragmentVMFactory(p2PRepository)
        generatingVideoFragmentVMFactory = GeneratingVideoFragmentVMFactory(p2PRepository)
    }
}