package com.example.tfgandroid

import android.app.Application
import android.os.Build
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.helpers.SharedPreferencesHelper

class MyApplication: Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        if (SharedPreferencesHelper.getUsername(this) == "") {
            SharedPreferencesHelper.setUsername(this, Build.MODEL)
        }
        appContainer = AppContainer(this)
    }

    fun startLateProperties(mainActivity: MainActivity) {
        appContainer.startLateProperties(mainActivity)
    }



}