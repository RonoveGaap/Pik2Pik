package com.example.tfgandroid.fragments.generatingVideoFragment

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.tfgandroid.AppContainer
import com.example.tfgandroid.MyApplication
import com.example.tfgandroid.R
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Image
import com.example.tfgandroid.fragments.galleryFragment.GalleryFragment
import com.example.tfgandroid.fragments.galleryFragment.GalleryFragmentVM
import com.example.tfgandroid.helpers.EnumHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class GeneratingVideoFragment : Fragment() {

    private lateinit var appContainer: AppContainer
    private lateinit var generatingVideoFragmentVM: GeneratingVideoFragmentVM
    private lateinit var lifecycleOwner: LifecycleOwner

    private lateinit var group: Group
    private lateinit var imageList: List<Image>

    // 0 = SIN EMPEZAR, 1 = GENERANDO, 2 = GENERADO
    private var currentlyGenerating = 0
    private var sessionStatus = EnumHelper.SessionStatus.UNDEFINED.status

    private lateinit var spinImageView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_generating_video, container, false)

        spinImageView = view.findViewById(R.id.generating_video_spin)

        val rotation = AnimationUtils.loadAnimation(requireContext(), R.anim.reload_groups)
        rotation.fillAfter = true
        spinImageView.startAnimation(rotation)

        appContainer = (requireActivity().application as MyApplication).appContainer
        generatingVideoFragmentVM = ViewModelProvider(this, appContainer.generatingVideoFragmentVMFactory)[GeneratingVideoFragmentVM::class.java]
        lifecycleOwner = this

        generatingVideoFragmentVM.getAllImagesFromGroup().observe(lifecycleOwner) { list ->
            imageList = list
            if (this::group.isInitialized && currentlyGenerating == 0) {
                generatingVideoFragmentVM.closeGroup()
                generateVideo()
            }
        }

        generatingVideoFragmentVM.getLastGroup().observe(lifecycleOwner) { mGroup ->
            group = mGroup
            if (this::imageList.isInitialized && currentlyGenerating == 0) {
                generatingVideoFragmentVM.closeGroup()
                generateVideo()
            }
        }

        generatingVideoFragmentVM.getSessionStatus().observe(lifecycleOwner) { status ->
            sessionStatus = status
            if (sessionStatus == EnumHelper.SessionStatus.FINISHED.status && currentlyGenerating == 2) {
                videoGenerated()
            }
        }


        return view
    }

    private fun videoGenerated()
    {
        spinImageView.clearAnimation()
    }

    private fun generateVideo()
    {
        currentlyGenerating = 1
        Log.d(GalleryFragment.TAG, group.getDirectory(requireContext()).absolutePath)
        var cmd = ""
        var afterCmd = "-filter_complex \""
        var finalCmd = ""
        imageList.forEachIndexed { index, image ->
            cmd += "-loop 1 -t 3 -i ${image.path}"
            cmd += " "
            if (index == 0) {
                afterCmd += "[0:v]scale=1024:576:force_original_aspect_ratio=decrease,pad=1024:576:(ow-iw)/2:(oh-ih)/2,trim=duration=3,fade=t=out:st=2.5:d=0.5[v0];"
            } else {
                afterCmd += "[$index:v]scale=1024:576:force_original_aspect_ratio=decrease,pad=1024:576:(ow-iw)/2:(oh-ih)/2,trim=duration=3,fade=t=in:st=0:d=0.5,fade=t=out:st=2.5:d=0.5[v$index];"
            }
            finalCmd += "[v$index]"
        }
        cmd = "${cmd}${afterCmd}${finalCmd}concat=n=${imageList.size}\" -preset ultrafast "
        cmd += "${group.getDirectory(requireContext()).absolutePath}/output.mp4"
        Log.d(GalleryFragment.TAG, cmd)
        val executionId: Long = FFmpeg.executeAsync(cmd) { executionId, returnCode ->
            Log.d(GalleryFragment.TAG, "Execution ID: $executionId")
            Log.d(GalleryFragment.TAG, "Return code: $returnCode")
            val videoFile = File("${group.getDirectory(requireContext()).absolutePath}/output.mp4")
            saveVideo(videoFile.absolutePath, videoFile.name)
            currentlyGenerating = 2
            generatingVideoFragmentVM.saveVideo()
            if (sessionStatus == EnumHelper.SessionStatus.FINISHED.status && currentlyGenerating == 2) {
                videoGenerated()
            }
        }
    }

    private fun saveVideo(filePath: String?, fileName: String) {
        filePath?.let {
            val context = requireContext()
            val values = ContentValues().apply {
                val folderName = Environment.DIRECTORY_MOVIES

                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.TITLE, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= 29) {
                    put(
                        MediaStore.Video.Media.RELATIVE_PATH,
                        folderName + "/${context.getString(R.string.app_name)}"
                    )
                    put(
                        MediaStore.Video.Media.DATE_ADDED,
                        System.currentTimeMillis() / 1000
                    )
                    put(MediaStore.Video.Media.IS_PENDING, 1)

                } else {
                    put(
                        MediaStore.Video.Media.DATE_ADDED,
                        System.currentTimeMillis() / 1000
                    )
                }
            }
            val fileUri = if (Build.VERSION.SDK_INT >= 29) {
                val collection =
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                context.contentResolver.insert(collection, values)
            } else {
                requireContext().contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values
                )!!
            }
            fileUri?.let {
                context.contentResolver.openFileDescriptor(fileUri, "w").use { descriptor ->
                    descriptor?.let {
                        try {
                            FileOutputStream(descriptor.fileDescriptor).use { out ->
                                val videoFile = File(filePath)
                                FileInputStream(videoFile).use { inputStream ->
                                    val buf = ByteArray(8192)
                                    while (true) {
                                        val sz = inputStream.read(buf)
                                        if (sz <= 0) break
                                        out.write(buf, 0, sz)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(GalleryFragment.TAG, e.message.toString())
                            return
                        }
                    }
                }

                values.clear()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                try {
                    requireContext().contentResolver.update(fileUri, values, null, null)
                }catch (e:Exception){
                    Log.e(GalleryFragment.TAG, e.message.toString())
                }
            }
        }
        Log.d(GalleryFragment.TAG, "Video guardado en la galería")
    }
}