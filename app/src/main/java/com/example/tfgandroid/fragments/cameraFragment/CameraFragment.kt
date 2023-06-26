package com.example.tfgandroid.fragments.cameraFragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.tfgandroid.AppContainer
import com.example.tfgandroid.MyApplication
import com.example.tfgandroid.R
import com.example.tfgandroid.activities.cameraActivity.CameraActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CameraFragment : Fragment() {

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

    private lateinit var appContainer: AppContainer
    private lateinit var cameraFragmentVM: CameraFragmentVM
    private lateinit var lifecycleOwner: LifecycleOwner

    private lateinit var textureView: TextureView
    private lateinit var cameraId: String
    private lateinit var backgroundHandlerThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var imageReader: ImageReader
    private lateinit var previewSize: Size
    private lateinit var videoSize: Size
    private var shouldProceedWithOnResume: Boolean = true
    private var orientations : SparseIntArray = SparseIntArray(4).apply {
        append(Surface.ROTATION_0, 0)
        append(Surface.ROTATION_90, 90)
        append(Surface.ROTATION_180, 180)
        append(Surface.ROTATION_270, 270)
    }

    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording: Boolean = false
    private lateinit var mCameraCharacteristics: CameraCharacteristics
    private var pictureRotation = 0

    private lateinit var lastImageView: ImageView

    companion object {
        const val TAG = "CameraFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        appContainer = (requireActivity().application as MyApplication).appContainer
        cameraFragmentVM = ViewModelProvider(this, appContainer.cameraFragmentVMFactory)[CameraFragmentVM::class.java]
        lifecycleOwner = this

        lastImageView = view.findViewById(R.id.camera_go_to_gallery_button)

        textureView = view.findViewById(R.id.texture_view)
        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        view.findViewById<ImageView>(R.id.take_photo_btn).apply {
            setOnClickListener {
                takePhoto()
            }
        }

        cameraFragmentVM.getLastImageFromGroup().observe(lifecycleOwner) { lastImage ->
            if (lastImage != null) {
                Glide.with(requireContext())
                    .load(lastImage.path)
                    .centerCrop()
                    .into(lastImageView)
            }
        }

//        view.findViewById<Button>(R.id.record_video_btn).apply {
//            setOnClickListener {
//                if (isRecording) {
//                    mediaRecorder.stop()
//                    mediaRecorder.reset()
//                } else {
//                    mediaRecorder = MediaRecorder()
//                    setupMediaRecorder()
//                    startRecording()
//                }
//
//                isRecording = !isRecording
//            }
//        }

        view.findViewById<ImageView>(R.id.camera_go_to_gallery_button).apply {
            setOnClickListener {
                (requireActivity() as CameraActivity).goToGallery()
            }
        }

        view.findViewById<ImageView>(R.id.camera_abandon_group_button).apply {
            setOnClickListener {
                (requireActivity() as CameraActivity).abandonGroup()
            }
        }

        ActivityCompat.requestPermissions(requireActivity(), permissions, 1)
        startBackgroundThread()

        return view
    }

    private fun setupCamera() {
        val cameraIds: Array<String> = cameraManager.cameraIdList

        for (id in cameraIds) {
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(id)

            //If we want to choose the rear facing camera instead of the front facing one
            if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                continue
            }

            mCameraCharacteristics = cameraCharacteristics

            val streamConfigurationMap : StreamConfigurationMap? = cameraCharacteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            if (streamConfigurationMap != null) {
                previewSize = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(
                    ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!
                videoSize = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(MediaRecorder::class.java).maxByOrNull { it.height * it.width }!!
                imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.JPEG, 1)
                imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
            }
            cameraId = id
        }
    }

    private fun wasCameraPermissionWasGiven() : Boolean {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            return true
        }

        return false
    }


    fun requestPermissionSuccess()
    {
        surfaceTextureListener.onSurfaceTextureAvailable(textureView.surfaceTexture!!, textureView.width, textureView.height)
    }

    private fun takePhoto() {
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder.addTarget(imageReader.surface)
//        val rotation = requireActivity().windowManager.defaultDisplay.rotation
//        val rotation = (requireActivity() as CameraActivity).getRotationAngle()

//        Log.d(TAG, rotation.toString())
//        Log.d(TAG, getJpegOrientation(rotation).toString())
//        captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(rotation))
//        captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(180))

//        captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation)

        pictureRotation = (requireActivity() as CameraActivity).getRotationAngle()

        cameraCaptureSession.capture(captureRequestBuilder.build(), captureCallback, null)
    }

    private fun getJpegOrientation(mDeviceOrientation: Int): Int
    {
        var deviceOrientation = mDeviceOrientation
        if (deviceOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) return 0
        val sensorOrientation =
            mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90

        // Reverse device orientation for front-facing cameras
        val facingFront =
            mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        if (facingFront) deviceOrientation = -deviceOrientation

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + deviceOrientation + 360) % 360
    }

    @SuppressLint("MissingPermission")
    private fun connectCamera() {
        cameraManager.openCamera(cameraId, cameraStateCallback, backgroundHandler)
    }

    private fun setupMediaRecorder() {
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setVideoSize(videoSize.width, videoSize.height)
        mediaRecorder.setVideoFrameRate(30)
        mediaRecorder.setOutputFile(createFile().absolutePath)
        mediaRecorder.setVideoEncodingBitRate(10_000_000)
        mediaRecorder.prepare()
    }

    private fun startRecording() {
        val surfaceTexture : SurfaceTexture? = textureView.surfaceTexture
        surfaceTexture?.setDefaultBufferSize(previewSize.width, previewSize.height)
        val previewSurface: Surface = Surface(surfaceTexture)
        val recordingSurface = mediaRecorder.surface
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        captureRequestBuilder.addTarget(previewSurface)
        captureRequestBuilder.addTarget(recordingSurface)

        cameraDevice.createCaptureSession(listOf(previewSurface, recordingSurface), captureStateVideoCallback, backgroundHandler)
    }

    /**
     * Surface Texture Listener
     */

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        @SuppressLint("MissingPermission")
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            setupCamera()
            connectCamera()
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {

        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {

        }
    }

    /**
     * Camera State Callbacks
     */

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            val surfaceTexture : SurfaceTexture? = textureView.surfaceTexture
            surfaceTexture?.setDefaultBufferSize(previewSize.width, previewSize.height)
            val previewSurface: Surface = Surface(surfaceTexture)

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(previewSurface)

            cameraDevice.createCaptureSession(listOf(previewSurface, imageReader.surface), captureStateCallback, null)
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {

        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            val errorMsg = when(error) {
                ERROR_CAMERA_DEVICE -> "Fatal (device)"
                ERROR_CAMERA_DISABLED -> "Device policy"
                ERROR_CAMERA_IN_USE -> "Camera in use"
                ERROR_CAMERA_SERVICE -> "Fatal (service)"
                ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                else -> "Unknown"
            }
            Log.e(CameraActivity.TAG, "Error when trying to connect camera $errorMsg")
        }
    }

    /**
     * Background Thread
     */
    private fun startBackgroundThread() {
        backgroundHandlerThread = HandlerThread("CameraVideoThread")
        backgroundHandlerThread.start()
        backgroundHandler = Handler(backgroundHandlerThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundHandlerThread.quitSafely()
        backgroundHandlerThread.join()
    }

    /**
     * Capture State Callback
     */

    private val captureStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {

        }
        override fun onConfigured(session: CameraCaptureSession) {
            cameraCaptureSession = session

            cameraCaptureSession.setRepeatingRequest(
                captureRequestBuilder.build(),
                null,
                backgroundHandler
            )
        }
    }

    private val captureStateVideoCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e(CameraActivity.TAG, "Configuration failed")
        }
        override fun onConfigured(session: CameraCaptureSession) {
            cameraCaptureSession = session
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureResult.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
            try {
                cameraCaptureSession.setRepeatingRequest(
                    captureRequestBuilder.build(), null,
                    backgroundHandler
                )
                mediaRecorder.start()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                Log.e(CameraActivity.TAG, "Failed to start camera preview because it couldn't access the camera")
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Capture Callback
     */
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureStarted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            timestamp: Long,
            frameNumber: Long
        ) {}

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) { }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {

        }
    }

    /**
     * ImageAvailable Listener
     */
    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image: Image = reader.acquireLatestImage()
        (requireActivity() as CameraActivity).savePicture(image, pictureRotation)
        image.close()
    }

    /**
     * File Creation
     */

    private fun createFile(): File {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
        return File(requireActivity().filesDir, "VID_${sdf.format(Date())}.mp4")
    }

}