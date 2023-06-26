package com.example.tfgandroid.fragments.myGroupsFragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgandroid.AppContainer
import com.example.tfgandroid.MyApplication
import com.example.tfgandroid.R
import com.example.tfgandroid.activities.mainActivity.MainActivity
import com.example.tfgandroid.adapters.MyGroupsAdapter
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.fragments.groupSearch.GroupSearchFragmentVM
import com.example.tfgandroid.fragments.myGroupDetailFragment.MyGroupDetailFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyGroupsFragment : Fragment() {

    private lateinit var appContainer: AppContainer
    private lateinit var myGroupsFragmentVM: MyGroupsFragmentVM
    private lateinit var lifecycleOwner: LifecycleOwner

    private var groups = listOf<Group>()
    private lateinit var myGroupsRV: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_groups, container, false)

        myGroupsRV = view.findViewById(R.id.my_groups_recycler_view)

        appContainer = (requireActivity().application as MyApplication).appContainer
        myGroupsFragmentVM = ViewModelProvider(this, appContainer.myGroupsFragmentVMFactory)[MyGroupsFragmentVM::class.java]
        lifecycleOwner = this

        view.findViewById<ImageView>(R.id.my_groups_back_button).setOnClickListener {
            requireActivity().onBackPressed()
        }

        myGroupsFragmentVM.getAllGroups().observe(lifecycleOwner) { pGroups ->
            groups = pGroups
            initializeList()
        }

        return view
    }

    private fun initializeList()
    {
        myGroupsRV.layoutManager = LinearLayoutManager(requireContext())
        val adapter = MyGroupsAdapter(groups)
        myGroupsRV.adapter = adapter
        adapter.onItemClick = {group ->
            (requireActivity() as MainActivity).groupDetails(group.id!!)
        }
        adapter.onItemDownload = {group ->
            (requireActivity() as MainActivity).goToVideoPlayer(group.id!!)
        }
        adapter.onItemDelete = {group ->
            openDeleteDialog(group.id!!)
        }
    }

    private fun openDeleteDialog(groupId: Long)
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
                myGroupsFragmentVM.deleteGroup(groupId)
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    companion object {
        const val TAG = "MyGroupsFragment"
    }
}