package com.example.tfgandroid.activities.cameraActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.tfgandroid.AppContainer
import com.example.tfgandroid.MyApplication
import com.example.tfgandroid.R
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.activities.mainActivity.MainActivityVM
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.fragments.cameraFragment.CameraFragment
import com.example.tfgandroid.fragments.galleryFragment.GalleryFragment
import com.example.tfgandroid.fragments.generatingVideoFragment.GeneratingVideoFragment
import com.example.tfgandroid.helpers.EnumHelper
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.atan2


class CameraActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        const val TAG = "CameraActivity"
    }

    private lateinit var appContainer: AppContainer
    private lateinit var cameraActivityVM: CameraActivityVM
    private lateinit var lifecycleOwner: LifecycleOwner

    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentPlaceholder: FrameLayout

    private lateinit var cameraFragment: CameraFragment
    private lateinit var galleryFragment: GalleryFragment
    private lateinit var generatingVideoFragment: GeneratingVideoFragment
    private var processingImage = ArrayList<Image>()

    private lateinit var sManager: SensorManager
    private lateinit var sensor: Sensor
    private var rotationAngle = 0

    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        appContainer = (application as MyApplication).appContainer
        cameraActivityVM = ViewModelProvider(this, appContainer.cameraActivityVMFactory)[CameraActivityVM::class.java]
        lifecycleOwner = this

        cameraActivityVM.getSessionStatus().observe(lifecycleOwner) { status ->
            if (status == EnumHelper.SessionStatus.LEFT.status || status == EnumHelper.SessionStatus.FINISHED.status) {
                if (counter == 0) {
                    counter++
                    Log.d(TAG, "Iniciamos la actividad MAIN")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("comesFromSession", true)
                    startActivity(intent)
                }
            }
        }

        fragmentPlaceholder = findViewById(R.id.camera_fragment_placeholder)
        fragmentManager = supportFragmentManager

        sManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        cameraFragment = CameraFragment()
        galleryFragment = GalleryFragment()
        generatingVideoFragment = GeneratingVideoFragment()
        changeCurrentFragment(cameraFragment, false)
    }

    override fun onResume() {
        super.onResume()
        sManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }


    fun savePicture(image: Image, rotation: Int): LiveData<Int>
    {
        processingImage.add(image)
        cameraActivityVM.saveImage(image, rotation)
        return cameraActivityVM.getImagedAddedCallback()
    }

    private fun changeCurrentFragment(fragment: Fragment, back: Boolean = false) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.camera_fragment_placeholder, fragment)
        if (!back) {
            transaction.addToBackStack(fragment.tag)
        }
        transaction.commit()
    }

    fun goToGallery()
    {
        changeCurrentFragment(galleryFragment)
    }

    fun getRotationAngle(): Int
    {
        return rotationAngle
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(CameraFragment.TAG, "Entramos en la petición de permisos")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraFragment.requestPermissionSuccess()
//            surfaceTextureListener.onSurfaceTextureAvailable(textureView.surfaceTexture!!, textureView.width, textureView.height)
        } else {
//            Toast.makeText(
//                this,
//                "Camera permission is needed to run this application",
//                Toast.LENGTH_LONG
//            )
//                .show()
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )) {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", this.packageName, null)
                startActivity(intent)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val aX = event.values[0].toDouble()
            val aY = event.values[1].toDouble()
            val angle = atan2(aX, aY) / (Math.PI / 180)
//            Log.d(TAG, "El ángulo es $angle")
            rotationAngle = angle.toInt()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    fun abandonGroup()
    {
        cameraActivityVM.abandonGroup()
    }

    fun startGeneratingVideo()
    {
        changeCurrentFragment(generatingVideoFragment)
    }

//    @SuppressLint("MissingPermission")
//    override fun onResume() {
//        super.onResume()
//        startBackgroundThread()
//        if (textureView.isAvailable && shouldProceedWithOnResume) {
//            setupCamera()
//        } else if (!textureView.isAvailable){
//            textureView.surfaceTextureListener = surfaceTextureListener
//        }
//        shouldProceedWithOnResume = !shouldProceedWithOnResume
//    }


}