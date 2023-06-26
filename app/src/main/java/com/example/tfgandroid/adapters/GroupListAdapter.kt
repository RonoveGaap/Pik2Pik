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

class GroupListAdapter(
    private val dataSet: ArrayList<GroupFound>,
    private val context: Context,
    var onItemClick: ((GroupFound) -> Unit)? = null,
    private var selectedIndex: Int = -1

): RecyclerView.Adapter<GroupListAdapter.ViewHolder>() {


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val groupName: TextView
        val groupOwner: TextView
        val groupSize: TextView
        val groupState: TextView
        val container: ConstraintLayout

        init {
            groupName = view.findViewById(R.id.groups_found_list_item_name)
            groupOwner = view.findViewById(R.id.groups_found_list_item_owner)
            groupSize = view.findViewById(R.id.groups_found_list_item_size)
            groupState = view.findViewById(R.id.groups_found_list_item_state)
            container = view.findViewById(R.id.groups_found_item_container)
            view.setOnClickListener {
                if (selectedIndex == adapterPosition) {
                    onItemClick?.invoke(dataSet[adapterPosition])
                    selectedIndex = -1
                } else {
                    selectedIndex = adapterPosition
                    onItemClick?.invoke(dataSet[adapterPosition])
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.groups_found_list_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.groupName.text = dataSet[position].groupName
        viewHolder.groupOwner.text = dataSet[position].groupOwner
        viewHolder.groupSize.text = dataSet[position].size


        viewHolder.groupState.text = when (dataSet[position].status) {
            EnumHelper.SessionStatus.UNDEFINED.status -> "Esperando"
            EnumHelper.SessionStatus.STARTING.status -> "Iniciando"
            EnumHelper.SessionStatus.STARTED.status -> "Iniciado"
            EnumHelper.SessionStatus.FINISHED.status -> "Finalizado"
            else -> "Esperando"
        }

        if (selectedIndex == position) {
            viewHolder.container.setBackgroundResource(R.drawable.orange_gradient_with_border_button_background)
//            viewHolder.groupName.setBackgroundResource(R.color.test_blue)
//            viewHolder.groupOwner.setBackgroundResource(R.color.test_blue)
//            viewHolder.groupSize.setBackgroundResource(R.color.test_blue)
        } else {
            viewHolder.container.setBackgroundResource(R.drawable.orange_gradient_button_background)
        }
    }

    override fun getItemCount() = dataSet.size

    fun reloadData()
    {
        notifyDataSetChanged()
    }

}