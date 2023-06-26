package com.example.tfgandroid.fragments.galleryFragment

import android.app.Dialog
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.tfgandroid.AppContainer
import com.example.tfgandroid.MyApplication
import com.example.tfgandroid.R
import com.example.tfgandroid.activities.cameraActivity.CameraActivity
import com.example.tfgandroid.adapters.GalleryImageAdapter
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class GalleryFragment : Fragment() {

    private lateinit var appContainer: AppContainer
    private lateinit var galleryFragmentVM: GalleryFragmentVM
    private lateinit var lifecycleOwner: LifecycleOwner

    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var imageList: List<Image>

    private lateinit var createVideoButton: ConstraintLayout
    private lateinit var backButton: ImageView
    private lateinit var group: Group

    private lateinit var createVideoButtonText: TextView

    companion object {
        const val TAG = "GalleryFragment"
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        imageRecyclerView = view.findViewById(R.id.gallery_fragment_image_list)
        createVideoButton = view.findViewById(R.id.fragment_gallery_generate_video_button)
        backButton = view.findViewById(R.id.gallery_back_button)
        createVideoButtonText = view.findViewById(R.id.fragment_gallery_generate_video_button_text)

        appContainer = (requireActivity().application as MyApplication).appContainer
        galleryFragmentVM = ViewModelProvider(this, appContainer.galleryFragmentVMFactory)[GalleryFragmentVM::class.java]
        lifecycleOwner = this

        galleryFragmentVM.getAllImagesFromGroup().observe(lifecycleOwner) { list ->
            imageList = list
            if (imageList.isEmpty() || imageList.any { image -> image.path == null }) {
                createVideoButtonText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_1))
            } else {
                createVideoButtonText.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange_1))
            }
            initializeImageList()
        }

        galleryFragmentVM.getLastGroup().observe(lifecycleOwner) { mGroup ->
            group = mGroup
        }

        createVideoButton.setOnClickListener {
            if (imageList.isNotEmpty() && !imageList.any { image -> image.path == null }) {
                openDialog()
            }
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

    private fun initializeImageList() {
        imageRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = GalleryImageAdapter(imageList, requireContext())
        imageRecyclerView.adapter = adapter
    }

    private fun openDialog()
    {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.confirm_generate_video_dialog)

        dialog.findViewById<ConstraintLayout>(R.id.confirm_generate_video_cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<ConstraintLayout>(R.id.confirm_generate_video_accept_button).setOnClickListener {
            (requireActivity() as CameraActivity).startGeneratingVideo()
            dialog.dismiss()
        }
        dialog.show()
    }


}