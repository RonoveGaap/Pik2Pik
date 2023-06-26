package com.example.tfgandroid.adapters

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgandroid.R
import com.example.tfgandroid.database.models.GroupMember
import com.example.tfgandroid.database.models.Guest

class GroupGuestListAdapter(
    private val dataSet: List<GroupMember>,
    private val context: Context,
    private val myName: String,
    var onItemClick: ((GroupMember) -> Unit)? = null,

): RecyclerView.Adapter<GroupGuestListAdapter.ViewHolder>() {


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textView: TextView
        val roleView: TextView
        val hostIcon: ImageView
        init {
            textView = view.findViewById(R.id.group_guest_item_username)
            roleView = view.findViewById(R.id.group_guest_item_role)
            hostIcon = view.findViewById(R.id.group_guest_item_host_icon)
            view.setOnClickListener {
                onItemClick?.invoke(dataSet[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.group_guest_list_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.roleView.text = if (dataSet[position].isHost) "Host" else "Invitado"
        viewHolder.hostIcon.visibility = if (dataSet[position].name == myName) View.VISIBLE else View.INVISIBLE
        viewHolder.textView.text = dataSet[position].name
    }

    override fun getItemCount() = dataSet.size


}