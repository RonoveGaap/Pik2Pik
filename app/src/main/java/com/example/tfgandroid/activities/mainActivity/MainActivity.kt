package com.example.tfgandroid.activities.mainActivity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgandroid.AppContainer
import com.example.tfgandroid.MyApplication
import com.example.tfgandroid.R
import com.example.tfgandroid.activities.cameraActivity.CameraActivity
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.fragments.CreateGroupFragment
import com.example.tfgandroid.fragments.groupDetails.GroupDetailsFragment
import com.example.tfgandroid.fragments.StartFragment
import com.example.tfgandroid.fragments.configurationFragment.ConfigurationFragment
import com.example.tfgandroid.fragments.groupSearch.GroupSearchFragment
import com.example.tfgandroid.fragments.myGroupDetailFragment.MyGroupDetailFragment
import com.example.tfgandroid.fragments.myGroupsFragment.MyGroupsFragment
import com.example.tfgandroid.fragments.videoPlayerFragment.VideoPlayerFragment
import com.example.tfgandroid.helpers.DateHelper
import com.example.tfgandroid.helpers.SharedPreferencesHelper
import kotlin.math.atan2

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
    )


    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentPlaceholder: FrameLayout
    private lateinit var backgroundFocus: ConstraintLayout
    private lateinit var p2pList: RecyclerView

    private lateinit var appContainer: AppContainer
    private lateinit var mainActivityVM: MainActivityVM
    private lateinit var lifecycleOwner: LifecycleOwner


    private var startFragment = StartFragment()
    private var createGroupFragment = CreateGroupFragment()
    private lateinit var groupDetailsFragment: GroupDetailsFragment
    private lateinit var groupSearchFragment: GroupSearchFragment
    private lateinit var myGroupsFragment: MyGroupsFragment
    private lateinit var myGroupDetailFragment: MyGroupDetailFragment
    private lateinit var videoPlayerFragment: VideoPlayerFragment
    private lateinit var configurationFragment: ConfigurationFragment

    private lateinit var mainActivityBackgroundSpin: ImageView
    private lateinit var mainActivityBackgroundText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainActivityBackgroundSpin = findViewById(R.id.main_activity_background_spin)
        mainActivityBackgroundText = findViewById(R.id.main_activity_background_text)

        appContainer = (application as MyApplication).appContainer
        appContainer.startLateProperties(this)
        mainActivityVM = ViewModelProvider(this, appContainer.mainActivityVMFactory)[MainActivityVM::class.java]
        lifecycleOwner = this

        ActivityCompat.requestPermissions(this, permissions, 1)

        fragmentPlaceholder = findViewById(R.id.fragment_placeholder)
        backgroundFocus = findViewById(R.id.main_activity_background_focus)
        p2pList = findViewById(R.id.p2p_devices_list)
        fragmentManager = supportFragmentManager
        goToMain(true)
    }

    private fun changeCurrentFragment(fragment: Fragment, back: Boolean = false) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_placeholder, fragment)
        if (!back) {
            transaction.addToBackStack(fragment.tag)
        }
        transaction.commit()
    }

    fun createGroup() {
        changeCurrentFragment(createGroupFragment)
    }

    fun goToMain(goingBack: Boolean) {
        changeCurrentFragment(startFragment, goingBack)
    }

    fun goToGroupJoinedScreen()
    {
        groupDetailsFragment = GroupDetailsFragment()
        val bundle = Bundle()
        bundle.putBoolean("isHost", false)
        groupDetailsFragment.arguments = bundle
        changeCurrentFragment(groupDetailsFragment, false)
    }

    fun searchGroups() {
        groupSearchFragment = GroupSearchFragment()
        changeCurrentFragment(groupSearchFragment, false)
    }

    fun groupCreated(group: Group) {
        mainActivityVM.createGroup(group)
        groupDetailsFragment = GroupDetailsFragment()
        val bundle = Bundle()
        bundle.putBoolean("isHost", true)
        groupDetailsFragment.arguments = bundle
        changeCurrentFragment(groupDetailsFragment, false)
    }

    fun goToMyGroups()
    {
        myGroupsFragment = MyGroupsFragment()
        changeCurrentFragment(myGroupsFragment, false)
    }

    fun groupDetails(groupId: Long)
    {
        myGroupDetailFragment = MyGroupDetailFragment()
        val bundle = Bundle()
        bundle.putLong("groupId", groupId)
        myGroupDetailFragment.arguments = bundle
        changeCurrentFragment(myGroupDetailFragment, false)
    }

    fun goToVideoPlayer(groupId: Long)
    {
        videoPlayerFragment = VideoPlayerFragment()
        val bundle = Bundle()
        bundle.putLong("groupId", groupId)
        videoPlayerFragment.arguments = bundle
        changeCurrentFragment(videoPlayerFragment, false)
    }

    fun goToConfiguration()
    {
        configurationFragment = ConfigurationFragment()
        changeCurrentFragment(configurationFragment, false)
    }

    fun goToCamera()
    {
        hideMessageLoading()
        startActivity(Intent(this, CameraActivity::class.java))
    }

    fun hideMessageLoading()
    {
        backgroundFocus.visibility = View.INVISIBLE
        mainActivityBackgroundSpin.clearAnimation()
    }

    fun showMessageLoading(text: String)
    {
        val rotation = AnimationUtils.loadAnimation(this, R.anim.reload_groups)
        rotation.fillAfter = true
        mainActivityBackgroundSpin.startAnimation(rotation)
        mainActivityBackgroundText.text = text
        backgroundFocus.visibility = View.VISIBLE
    }
}