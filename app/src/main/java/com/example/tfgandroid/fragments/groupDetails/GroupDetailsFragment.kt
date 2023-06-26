package com.example.tfgandroid.fragments.groupDetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.example.tfgandroid.activities.mainActivity.MainActivityVM
import com.example.tfgandroid.adapters.GroupGuestListAdapter
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.GroupMember
import com.example.tfgandroid.database.models.Guest
import com.example.tfgandroid.helpers.EnumHelper


class GroupDetailsFragment : Fragment() {

    private lateinit var appContainer: AppContainer
    private lateinit var groupDetailsFragmentVM: GroupDetailsFragmentVM
    private lateinit var lifecycleOwner: LifecycleOwner

    private lateinit var group: Group
    private lateinit var groupNameTextView: TextView
    private lateinit var startSessionButton: ConstraintLayout
    private lateinit var startingMessage: TextView

    private lateinit var groupMembers: List<GroupMember>
    private var guestsCounter = 0
    private lateinit var guestsTextView: TextView
    private lateinit var guestRecyclerView: RecyclerView

    private lateinit var startSessionButtonText: TextView

    private var isHost = false
    private var myName = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_group_details, container, false)

        groupNameTextView = view.findViewById(R.id.group_details_group_name)
        guestsTextView = view.findViewById(R.id.group_details_group_size)
        guestRecyclerView = view.findViewById(R.id.group_details_members_list)
        startSessionButton = view.findViewById(R.id.group_details_start_session_button)
        startingMessage = view.findViewById(R.id.group_details_group_starting_message)
        startSessionButtonText = view.findViewById(R.id.group_details_start_session_button_text)

        appContainer = (requireActivity().application as MyApplication).appContainer
        groupDetailsFragmentVM = ViewModelProvider(this, appContainer.groupDetailsFragmentVMFactory)[GroupDetailsFragmentVM::class.java]
        lifecycleOwner = this

        myName = groupDetailsFragmentVM.getMyName()

        isHost = arguments?.getBoolean("isHost") == true

        if (isHost) {
            startSessionButton.visibility = View.VISIBLE
            startSessionButton.setOnClickListener {
                if (groupMembers.size > 1) {
                    (requireActivity() as MainActivity).showMessageLoading("Iniciando sesión")
                    groupDetailsFragmentVM.startSession()
                }
            }
        } else {
            startingMessage.visibility = View.VISIBLE
        }


        groupDetailsFragmentVM.getLastGroup().observe(lifecycleOwner) { mGroup ->
            group = mGroup
            groupNameTextView.text = group.name
            groupDetailsFragmentVM.getActiveMembersOfGroup(group.id!!).observe(lifecycleOwner) {mGuests ->
                groupMembers = mGuests
                if (groupMembers.size > 1) {
                    startSessionButtonText.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange_1))
                } else {
                    startSessionButtonText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_1))
                }
                initializeGuestList()
            }
        }

        groupDetailsFragmentVM.getSessionStatus().observe(lifecycleOwner) { status ->
            if (status == EnumHelper.SessionStatus.STARTED.status) {
                (requireActivity() as MainActivity).goToCamera()
            }
        }
        return view
    }

    private fun initializeGuestList() {
        guestRecyclerView.layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                lp.height = (height * 0.3).toInt()
                return true
            }
        }
        val adapter = GroupGuestListAdapter(groupMembers, requireContext(), myName)
        guestRecyclerView.adapter = adapter
        adapter.onItemClick = {guest ->
            Log.d(TAG, "Click sobre el guest ${guest.name}")
        }
        guestsTextView.text = groupMembers.size.toString()
    }

    companion object {
        const val TAG = "GroupDetailsFragment"
    }

}