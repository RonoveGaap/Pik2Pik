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
import com.example.tfgandroid.database.models.Guest
import com.example.tfgandroid.helpers.EnumHelper
import com.example.tfgandroid.p2p.models.GroupFound

class MyGroupsAdapter(
    private val dataSet: List<Group>,
    var onItemClick: ((Group) -> Unit)? = null,
    var onItemDownload: ((Group) -> Unit)? = null,
    var onItemDelete: ((Group) -> Unit)? = null,
): RecyclerView.Adapter<MyGroupsAdapter.ViewHolder>() {


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val groupName: TextView
        val groupDate: TextView
        private val downloadButton: ConstraintLayout
        private val deleteButton: ConstraintLayout

        init {
            groupName = view.findViewById(R.id.my_groups_item_group_name)
            groupDate = view.findViewById(R.id.my_groups_item_group_date)
            downloadButton = view.findViewById(R.id.my_groups_download_button)
            deleteButton = view.findViewById(R.id.my_groups_delete_button)
            view.setOnClickListener {
                onItemClick?.invoke(dataSet[adapterPosition])
            }
            downloadButton.setOnClickListener {
                onItemDownload?.invoke(dataSet[adapterPosition])
            }
            deleteButton.setOnClickListener {
                onItemDelete?.invoke(dataSet[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.my_groups_list_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.groupName.text = dataSet[position].name
        viewHolder.groupDate.text = dataSet[position].createdAt.split(' ')[0]
    }

    override fun getItemCount() = dataSet.size

}