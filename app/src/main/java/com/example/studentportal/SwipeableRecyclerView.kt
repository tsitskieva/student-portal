package com.example.studentportal

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SwipeableRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
    var onDeleteIconClickListener: ((View) -> Unit)? = null

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_UP) {
            findChildViewUnder(e.x, e.y)?.let { child ->
                val holder = getChildViewHolder(child) as SelectedGroupsAdapter.SelectedGroupViewHolder
                val iconX = e.x - (child.right - holder.offButton.width - 30.dpToPx())
                if (holder.offButton.visibility == View.VISIBLE &&
                    iconX in 0f..holder.offButton.width.toFloat() &&
                    e.y >= holder.offButton.top && e.y <= holder.offButton.bottom) {
                    onDeleteIconClickListener?.invoke(child)
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(e)
    }

    private fun Int.dpToPx(): Float =
        this * Resources.getSystem().displayMetrics.density
}