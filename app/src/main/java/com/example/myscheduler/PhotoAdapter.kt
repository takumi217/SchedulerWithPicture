package com.example.myscheduler

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmList
import io.realm.RealmRecyclerViewAdapter

class PhotoAdapter(photoList: OrderedRealmCollection<Photo>) :
    RealmRecyclerViewAdapter<Photo, PhotoAdapter.ViewHolder>(photoList, true) {

    private var listener: ((Long?) -> Unit)? = null
    fun setOnItemClickListener(listener:(Long?) -> Unit) {
        this.listener = listener
    }

    init {
        setHasStableIds(true)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.simple_photo, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val photo: Photo? = getItem(position)
        viewHolder.textView.text = "写真${position+1}"
        viewHolder.textView.setOnClickListener {
            listener?.invoke(photo?.id)
        }
    }


    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: 0
    }
}
