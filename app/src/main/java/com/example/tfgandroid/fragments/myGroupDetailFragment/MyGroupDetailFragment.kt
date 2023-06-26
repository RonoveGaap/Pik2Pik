package com.example.tfgandroid.fragments.myGroupDetailFragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgandroid.AppContainer
import com.example.tfgandroid.MyApplication
import com.example.tfgandroid.R
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.adapters.MyGroupImagesAdapter
import com.example.tfgandroid.adapters.MyGroupMembersAdapter
import com.example.tfgandroid.fragments.myGroupsFragment.MyGroupsFragmentVM
import com.example.tfgandroid.models.MyGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyGroupDetailFragment : Fragment() {

    private lateinit var appContainer: AppContainer
    private lateinit var myGroupDetailFragmentVM: MyGroupDetailFragmentVM
    private lateinit var lifecycleOwner: LifecycleOwner

    private lateinit var myGroup: MyGroup

    private lateinit var groupNameTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var imageNumberTextView: TextView
    private lateinit var hasVideoTextView: TextView

    private lateinit var membersRV: RecyclerView
    private lateinit var imagesRV: RecyclerView
    private var groupId = 0L


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_group_detail, container, false)

        appContainer = (requireActivity().application as MyApplication).appContainer
        myGroupDetailFragmentVM = ViewModelProvider(this, appContainer.myGroupDetailFragmentVMFactory)[MyGroupDetailFragmentVM::class.java]
        lifecycleOwner = this

        dateTextView = view.findViewById(R.id.my_group_detail_date)
        groupNameTextView = view.findViewById(R.id.my_group_detail_title)
        imageNumberTextView = view.findViewById(R.id.my_group_detail_number_of_images)
        hasVideoTextView = view.findViewById(R.id.my_group_detail_is_video_generated)

        membersRV = view.findViewById(R.id.my_group_detail_member_recycler_view)
        imagesRV = view.findViewById(R.id.my_group_detail_image_recycler_view)

        groupId = arguments?.getLong("groupId", 0L)!!

        if (groupId != 0L) {
            CoroutineScope(Dispatchers.IO).launch {
                myGroup = myGroupDetailFragmentVM.getGroupDetails(groupId!!)
                setGroupDataInView()
                initializeLists()
            }
        } else {
            requireActivity().onBackPressed()
        }

        view.findViewById<ImageView>(R.id.my_group_detail_back_button).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<ConstraintLayout>(R.id.my_group_detail_view_video_button).setOnClickListener {
            (requireActivity() as MainActivity).goToVideoPlayer(groupId!!)
        }

        view.findViewById<ConstraintLayout>(R.id.my_group_detail_delete_group_button).setOnClickListener {
            openDeleteDialog()
        }

        return view
    }

    private fun setGroupDataInView()
    {
        groupNameTextView.text = myGroup.name
        dateTextView.text = myGroup.createdAt.split(' ')[0]
        imageNumberTextView.text = myGroup.images.size.toString()
        if (myGroup.video != null) {
            hasVideoTextView.text = "Sí"
        } else {
            hasVideoTextView.text = "No"
        }
    }

    private fun initializeLists()
    {
        membersRV.layoutManager = LinearLayoutManager(requireContext())
        membersRV.adapter = MyGroupMembersAdapter(myGroup.groupMembers)

        imagesRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        imagesRV.adapter = MyGroupImagesAdapter(myGroup.images, requireContext())
    }

    private fun openDeleteDialog()
    {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.confirm_delete_group_dialog)

        dialog.findViewById<ConstraintLayout>(R.id.delete_group_cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<ConstraintLayout>(R.id.delete_group_accept_button).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                myGroupDetailFragmentVM.deleteGroup(groupId)
            }
            dialog.dismiss()
            requireActivity().onBackPressed()
        }
        dialog.show()
    }

    companion object {
        const val TAG = "MyGroupDetailFragment"
    }
}