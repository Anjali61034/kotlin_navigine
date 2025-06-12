package com.example.canary.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.view.MotionEvent
import android.widget.Toast
import com.navigine.idl.java.SignalMeasurement
import com.example.canary.R
import java.util.*

abstract class DebugAdapterBaseExpanded<T : DebugViewHolderBase, V : SignalMeasurement> :
    DebugAdapterBase<T, V>() {

    companion object {
        const val LIST_SIZE_DEFAULT = 6
    }

    protected var expand = false
    private val copyTextBuilder = StringBuilder()

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: T, position: Int) {
        holder.title?.let { titleView ->
            // Set the arrow drawable
            if (mCurrentList.size <= LIST_SIZE_DEFAULT) {
                titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            } else {
                val drawableRes = if (expand) R.drawable.ic_arrow_circle_up else R.drawable.ic_arrow_circle_down
                titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
            }

            titleView.setOnTouchListener { v, event ->
                val paddingEnd = titleView.totalPaddingEnd
                val width = titleView.width

                if (event.x >= width - paddingEnd) {
                    // Check if drawable is present
                    titleView.compoundDrawables[2]?.let {
                        if (event.action == MotionEvent.ACTION_UP) {
                            isPressed = false
                            expand = !expand
                            mRecyclerView?.scheduleLayoutAnimation()
                            v.performClick()
                            notifyDataSetChanged()
                        }
                    }
                } else {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        v.postDelayed({
                            isPressed = !isRootScrolling
                            if (isPressed) {
                                mGestureDetector?.onTouchEvent(event)  // safe call added
                            }
                        }, 300)
                    }
                }
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return if (!expand) LIST_SIZE_DEFAULT else mCurrentList.size + 1
    }

    override fun submit(list: List<V>) {
        if (!isPressed) {
            mCurrentList.clear()
            mCurrentList.addAll(list)
            if (mCurrentList.size <= LIST_SIZE_DEFAULT) {
                expand = false
            }
            notifyDataSetChanged()
        }
    }

    override fun onCopyContent() {
        copyTextBuilder.setLength(0)
        for (signalMeasurement in mCurrentList) {
            copyTextBuilder.append(signalMeasurement.id)
                .append(" ")
                .append(String.format(Locale.ENGLISH, "%.1f", signalMeasurement.rssi))
                .append("  ")
                .append(String.format(Locale.ENGLISH, "%.1fm", signalMeasurement.distance))
                .append('\n')
        }

        val clip = ClipData.newPlainText("list content", copyTextBuilder.toString())
        mClipboardManager?.setPrimaryClip(clip)
        Toast.makeText(mContext, R.string.debug_copy_list_content, Toast.LENGTH_SHORT).show()
    }
}
