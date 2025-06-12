package com.example.canary.fragments

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.os.Vibrator
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresPermission
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView

abstract class DebugAdapterBase<T : RecyclerView.ViewHolder, V> : RecyclerView.Adapter<T>() {

    companion object {
        const val VIBRATION_DELAY = 75
        private const val TYPE_ROUNDED_TOP = 0
        private const val TYPE_ROUNDED_BOTTOM = 1
        private const val TYPE_RECT = 2

        @JvmStatic
        var isRootScrolling = false

        @JvmStatic
        fun setRootView(view: View) {
            if (view is NestedScrollView) {
                view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                    isRootScrolling = kotlin.math.abs(scrollY - oldScrollY) > 2
                })
            }
        }
    }

    protected var mContext: Context? = null
    protected var mRecyclerView: RecyclerView? = null
    protected var mClipboardManager: ClipboardManager? = null
    protected var mGestureDetector: GestureDetector? = null
    private var mVibrator: Vibrator? = null

    protected val mCurrentList: MutableList<V> = mutableListOf()

    protected var isPressed = false

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
        mContext = recyclerView.context
        mClipboardManager = mContext?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        mVibrator = mContext?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

        // Define gesture methods separately, then forward calls from interface
        val onDown: (MotionEvent) -> Boolean = {
            TODO("Not yet implemented")
        }

        val onShowPress: (MotionEvent) -> Unit = {
            TODO("Not yet implemented")
        }

        val onSingleTapUp: (MotionEvent) -> Boolean = {
            TODO("Not yet implemented")
        }

        val onScroll: (MotionEvent?, MotionEvent, Float, Float) -> Boolean = { _, _, _, _ ->
            TODO("Not yet implemented")
        }

        val onLongPress: (MotionEvent) -> Unit = {
            TODO("Not yet implemented")
        }

        val onFling: (MotionEvent?, MotionEvent, Float, Float) -> Boolean = { _, _, _, _ ->
            TODO("Not yet implemented")
        }

        mGestureDetector = GestureDetector(mContext, object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent): Boolean = onDown(e)
            override fun onShowPress(e: MotionEvent) = onShowPress(e)
            override fun onSingleTapUp(e: MotionEvent): Boolean = onSingleTapUp(e)
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean =
                onScroll(e1, e2, distanceX, distanceY)

            @RequiresPermission(Manifest.permission.VIBRATE)
            override fun onLongPress(e: MotionEvent) {
                onLongPress(e)
                onCopyContent()
                onVibrate()
                isPressed = false
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean =
                onFling(e1, e2, velocityX, velocityY)
        })
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_ROUNDED_TOP
            mCurrentList.size - 1 -> TYPE_ROUNDED_BOTTOM
            else -> TYPE_RECT
        }
    }

    override fun getItemCount(): Int = mCurrentList.size

    open fun submit(list: List<V>) {
        mCurrentList.clear()
        mCurrentList.addAll(list)
        notifyDataSetChanged()
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun onVibrate() {
        mVibrator?.vibrate(VIBRATION_DELAY.toLong())
    }

    protected abstract fun onCopyContent()
}
