package com.example.tfgandroid.adapters

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgandroid.R
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.GroupMember
import com.example.tfgandroid.database.models.Guest
import com.example.tfgandroid.helpers.EnumHelper
import com.example.tfgandroid.p2p.models.GroupFound

class MyGroupMembersAdapter(
    private val dataSet: ArrayList<GroupMember>,
): RecyclerView.Adapter<MyGroupMembersAdapter.ViewHolder>() {


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView

        init {
            name = view.findViewById(R.id.my_group_member_item_name)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.my_group_member_list_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.name.text = dataSet[position].name
    }

    override fun getItemCount() = dataSet.size

}