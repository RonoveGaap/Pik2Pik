package com.example.tfgandroid.fragments.groupSearch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgandroid.AppContainer
import com.example.tfgandroid.MyApplication
import com.example.tfgandroid.R
import com.example.tfgandroid.activities.cameraActivity.CameraActivity
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.adapters.GroupGuestListAdapter
import com.example.tfgandroid.adapters.GroupListAdapter
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.fragments.groupDetails.GroupDetailsFragment
import com.example.tfgandroid.fragments.groupDetails.GroupDetailsFragmentVM
import com.example.tfgandroid.helpers.EnumHelper
import com.example.tfgandroid.p2p.models.GroupFound

class GroupSearchFragment : Fragment() {

    private lateinit var appContainer: AppContainer
    private lateinit var groupSearchFragmentVM: GroupSearchFragmentVM
    private lateinit var lifecycleOwner: LifecycleOwner

    private var groupList: ArrayList<GroupFound> = arrayListOf()
    private lateinit var groupsTotalText: TextView
    private lateinit var groupRecyclerView: RecyclerView

    private lateinit var backButton: View
    private lateinit var joinButton: ConstraintLayout
    private lateinit var reloadButton: ImageView

    private var selectedGroup = ""



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_group_search, container, false)

        groupsTotalText = view.findViewById(R.id.group_search_group_count)
        groupRecyclerView = view.findViewById(R.id.groups_found_list)



        backButton = view.findViewById(R.id.group_search_back_button)
        joinButton = view.findViewById(R.id.group_search_join_button)
        reloadButton = view.findViewById(R.id.group_search_reload_icon)

        appContainer = (requireActivity().application as MyApplication).appContainer
        groupSearchFragmentVM = ViewModelProvider(this, appContainer.groupSearchFragmentVMFactory)[GroupSearchFragmentVM::class.java]
        lifecycleOwner = this
        groupsTotalText.text = requireContext().getString(R.string.group_search_group_count, groupList.size)

//        groupSearchFragmentVM.getGroups().observe(lifecycleOwner) { allGroups ->
//            groupList = allGroups
//            initializeGroupList()
//        }

        groupSearchFragmentVM.getFoundGroups().observe(lifecycleOwner) { allGroups ->
            groupList = allGroups
            reloadButton.clearAnimation()
            initializeGroupList()
        }

        groupSearchFragmentVM.getSessionStatus().observe(lifecycleOwner) { status ->
            if (status == EnumHelper.SessionStatus.STARTED.status) {
                startActivity(Intent(requireContext(), CameraActivity::class.java))
            }
        }

        groupSearchFragmentVM.groupJoined().observe(lifecycleOwner) { code ->
            if (code == EnumHelper.JoinGroupCode.SUCCESS.code) {
                (requireActivity() as MainActivity).hideMessageLoading()
                (requireActivity() as MainActivity).goToGroupJoinedScreen()
            }
        }

        backButton.setOnClickListener {
            groupSearchFragmentVM.stopSearchingGroups()
            requireActivity().onBackPressed()
        }

        joinButton.setOnClickListener {
            if (selectedGroup.isNotEmpty()) {
                (requireActivity() as MainActivity).showMessageLoading("Uniéndose a grupo")
                groupSearchFragmentVM.joinGroup(selectedGroup)
            }
        }

        reloadButton.setOnClickListener {
            val rotation = AnimationUtils.loadAnimation(requireContext(), R.anim.reload_groups)
            rotation.fillAfter = true
            reloadButton.startAnimation(rotation)
            groupList.clear()
            selectedGroup = ""
            joinButton.visibility = View.INVISIBLE
            initializeGroupList()
            groupSearchFragmentVM.searchGroup()
        }

        groupSearchFragmentVM.searchGroup()

        return view
    }

    private fun initializeGroupList() {
        groupRecyclerView.layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                lp.height = (height * 0.3).toInt()
                return true
            }
        }
        val adapter = GroupListAdapter(groupList, requireContext())
        groupRecyclerView.adapter = adapter
        adapter.onItemClick = {group ->
//            groupSearchFragmentVM.joinGroup(group.second)
            Log.d(TAG, "El endpoint seleccionado actualmente es $selectedGroup")
            Log.d(TAG, "Se pasa a seleccionar el siguiente endpoint ${group.endpointId}")
            if (selectedGroup.isNotEmpty() && selectedGroup == group.endpointId) {
                selectedGroup = ""
                joinButton.visibility = View.INVISIBLE
            } else {
                selectedGroup = group.endpointId
                joinButton.visibility = View.VISIBLE
            }
        }
        groupsTotalText.text = requireContext().getString(R.string.group_search_group_count, groupList.size)
    }

    companion object {
        const val TAG = "GroupSearchFragment"
    }
}