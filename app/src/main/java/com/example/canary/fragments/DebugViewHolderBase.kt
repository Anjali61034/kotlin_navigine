package com.example.canary.fragments

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.canary.R

open class DebugViewHolderBase(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var title: TextView? = null
    var uuid: TextView? = null
    var rssi: TextView? = null

    init {
        uuid = itemView.findViewById(R.id.li_debug__uuid)
        rssi = itemView.findViewById(R.id.li_debug__rssi)
    }
}
