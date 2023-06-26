package com.example.tfgandroid.adapters

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgandroid.R

class P2PDeviceAdapter(
    private val dataSet: Array<WifiP2pDevice>,
    private val context: Context,
    var onItemClick: ((WifiP2pDevice) -> Unit)? = null,

): RecyclerView.Adapter<P2PDeviceAdapter.ViewHolder>() {



    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textView: TextView
        init {
            textView = view.findViewById(R.id.p2p_list_item_name)
            view.setOnClickListener {
                onItemClick?.invoke(dataSet[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.p2p_device_list_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = context.getString(R.string.string_p2p_list_device_item, position + 1, dataSet[position].deviceName
        )
    }

    override fun getItemCount() = dataSet.size


}