package com.example.tfgandroid.adapters

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgandroid.R
import com.example.tfgandroid.database.models.Group
import com.example.tfgandroid.database.models.Guest
import com.example.tfgandroid.database.models.Image

class GalleryImageAdapter(
    private val dataSet: List<Image>,
    private val context: Context,
    var onItemClick: ((Image) -> Unit)? = null,
    private var selectedIndex: Int = -1

): RecyclerView.Adapter<GalleryImageAdapter.ViewHolder>() {


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageView: ImageView
        val textView: TextView
        val progress: ImageView

        init {
            imageView = view.findViewById(R.id.gallery_image_item_view)
            textView = view.findViewById(R.id.gallery_image_item_owner)
            progress = view.findViewById(R.id.photo_in_progress_spin)
            view.setOnClickListener {
                selectedIndex = adapterPosition
                onItemClick?.invoke(dataSet[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.gallery_image_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (dataSet[position].path != null) {
            Glide.with(context)
                .load(dataSet[position].path)
                .centerCrop()
                .into(viewHolder.imageView)
        } else {
            viewHolder.progress.visibility = View.VISIBLE
            val rotation = AnimationUtils.loadAnimation(context, R.anim.reload_groups)
            rotation.fillAfter = true
            viewHolder.progress.startAnimation(rotation)
        }
        viewHolder.textView.text = dataSet[position].owner
    }

    override fun getItemCount() = dataSet.size


}