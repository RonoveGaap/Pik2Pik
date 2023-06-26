package com.example.tfgandroid.fragments.videoPlayerFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import com.example.tfgandroid.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class VideoPlayerFragment : Fragment() {

    private lateinit var videoView: VideoView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_video_player, container, false)

        val groupId = arguments?.getLong("groupId", 0L)

        if (groupId != 0L) {
            val directory = File(requireContext().filesDir, "group_${groupId}")
            videoView = view.findViewById(R.id.video_player_view)
            val mediaController = MediaController(requireContext())
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)
            videoView.keepScreenOn = true
            videoView.setVideoPath("${directory.absolutePath}/output.mp4")
            videoView.start()
            videoView.requestFocus()
        } else {
            requireActivity().onBackPressed()
        }

        return view
    }
}