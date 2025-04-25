package com.example.canary.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.textview.MaterialTextView
import com.navigine.idl.java.Sublocation
import com.example.canary.R

class SublocationAdapter<T : Sublocation>(
    context: Context,
    resource: Int
) : ArrayAdapter<T>(context, resource) {

    private val currentList = mutableListOf<T>()

    private val TYPE_TOP = 0
    private val TYPE_MID = 1
    private val TYPE_BOTTOM = 2

    override fun getCount(): Int = currentList.size

    override fun getItem(position: Int): T = currentList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = when {
        position == 0 -> TYPE_TOP
        position == currentList.size - 1 -> TYPE_BOTTOM
        else -> TYPE_MID
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_sublocation, parent, false)

        (view as? MaterialTextView)?.text = currentList[position].name

        return view
    }

    fun submit(sublocations: List<T>) {
        currentList.clear()
        currentList.addAll(sublocations)
        notifyDataSetChanged()
    }
}