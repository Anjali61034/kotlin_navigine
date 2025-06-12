package com.example.canary.fragments

import android.view.View
import android.widget.TextView
import com.example.canary.R

class DebugViewHolderBaseBeacons(itemView: View) : DebugViewHolderBase(itemView) {

    public var distance: TextView? = null

    init {
        title = itemView.findViewById(R.id.li_debug_beacons__title)
        uuid = itemView.findViewById(R.id.li_debug_beacons__uuid)
        rssi = itemView.findViewById(R.id.li_debug_beacons__rssi)
        distance = itemView.findViewById(R.id.li_debug_beacons__distance)
    }
}
