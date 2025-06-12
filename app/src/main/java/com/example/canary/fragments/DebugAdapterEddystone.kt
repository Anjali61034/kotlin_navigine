package com.example.canary.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.navigine.idl.java.SignalMeasurement
import com.example.canary.R
import java.util.Locale

class DebugAdapterEddystone : DebugAdapterBaseExpanded<DebugViewHolderBaseBeacons, SignalMeasurement>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebugViewHolderBaseBeacons {
        val view: View = when (viewType) {
            0 -> LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_round_top_debug_beacons, parent, false)
            1 -> LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_round_bottom_debug_beacons, parent, false)
            else -> LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_debug_beacons, parent, false)
        }
        return DebugViewHolderBaseBeacons(view)
    }

    override fun onBindViewHolder(holder: DebugViewHolderBaseBeacons, position: Int) {
        if (position == 0) {
            holder.title?.text = String.format(
                Locale.ENGLISH,
                "EDDYSTONE (%d), entries/sec: %.1f",
                mCurrentList.size,
                mCurrentList.size.toFloat()
            )
            super.onBindViewHolder(holder, position)
        } else {
            try {
                val result = mCurrentList[position - 1]
                val ids = result.id.split(",")

                val address = ids[0].substring(1, 15 - ids[1].length) + "…, " +
                        ids[1].substring(0, ids[1].length - 1)

                holder.uuid?.text = address
                holder.rssi?.text = String.format(Locale.ENGLISH, "%.1f", result.rssi)
                holder.distance?.text = String.format(Locale.ENGLISH, "%.1fm", result.distance)
            } catch (e: IndexOutOfBoundsException) {
                holder.uuid?.text = "---"
                holder.rssi?.text = null
                holder.distance?.text = null
            }
        }
    }
}
