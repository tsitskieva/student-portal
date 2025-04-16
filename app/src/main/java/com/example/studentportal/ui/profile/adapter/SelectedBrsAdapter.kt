package com.example.studentportal.ui.profile.adapter

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.studentportal.R
import com.example.studentportal.data.model.Brs

class SelectedBrsAdapter(
    private var brsList: List<Brs>,
    private val onItemClick: (Brs) -> Unit,
    private val onDeleteClick: (Brs) -> Unit
) : RecyclerView.Adapter<SelectedBrsAdapter.SelectedBrsViewHolder>() {

    inner class SelectedBrsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val brsText: TextView = itemView.findViewById(R.id.brs_list_text)
        val onButton: ImageView = itemView.findViewById(R.id.brs_list_button_on)
        val offButton: ImageView = itemView.findViewById(R.id.brs_list_button_off)
        val activeLine: ImageView = itemView.findViewById(R.id.brs_list_line_active)
        val inactiveLine: ImageView = itemView.findViewById(R.id.brs_list_line_not_active)

        init {
            offButton.setOnClickListener {
                brsList.forEach { it.isActive = false }
                brsList[adapterPosition].isActive = true
                onItemClick(brsList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedBrsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.brs_in_list_settings, parent, false)
        return SelectedBrsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedBrsViewHolder, position: Int) {
        val currentItem = brsList[position]

        holder.brsText.text = currentItem.name

        if (currentItem.isActive) {
            holder.onButton.visibility = View.VISIBLE
            holder.offButton.visibility = View.GONE
            holder.activeLine.visibility = View.VISIBLE
            holder.inactiveLine.visibility = View.GONE
        } else {
            holder.onButton.visibility = View.GONE
            holder.offButton.visibility = View.VISIBLE
            holder.activeLine.visibility = View.GONE
            holder.inactiveLine.visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = brsList.size

    fun updateList(newList: List<Brs>) {
        brsList = newList.sortedByDescending { it.isActive }
        notifyDataSetChanged()
    }

    fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            private val deleteIcon = ContextCompat.getDrawable(recyclerView.context, R.drawable.ic_delete)
            private val intrinsicWidth = deleteIcon?.intrinsicWidth ?: 0
            private val intrinsicHeight = deleteIcon?.intrinsicHeight ?: 0
            private val background = RightRoundedRectBackground()
            private val cornerRadius = recyclerView.context.resources.getDimension(R.dimen.swipe_corner_radius)
            private var isFirstThresholdPassed = false
            private var firstThreshold = 0f
            private var originalDx = 0f

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val brsToDelete = brsList[position]
                onDeleteClick(brsToDelete)
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return if (!isFirstThresholdPassed) {
                    0.33f
                } else {
                    1f
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val isCanceled = dX == 0f && !isCurrentlyActive

                if (isCanceled) {
                    isFirstThresholdPassed = false
                    c.drawColor(Color.parseColor("#16181C"))
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                if (firstThreshold == 0f) {
                    firstThreshold = itemView.width * 0.33f
                }

                if (!isFirstThresholdPassed && Math.abs(dX) >= firstThreshold) {
                    isFirstThresholdPassed = true
                    originalDx = dX
                }

                val currentDx = if (!isFirstThresholdPassed) {
                    if (Math.abs(dX) > firstThreshold) {
                        if (dX < 0) -firstThreshold else firstThreshold
                    } else {
                        dX
                    }
                } else {
                    dX
                }

                background.draw(
                    c,
                    itemView.right + currentDx.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom,
                    cornerRadius
                )

                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin = (30 * recyclerView.context.resources.displayMetrics.density).toInt()
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicHeight

                deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                deleteIcon?.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, currentDx, dY, actionState, isCurrentlyActive)
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                isFirstThresholdPassed = false
                firstThreshold = 0f
                originalDx = 0f
            }

            private fun clearCanvas(c: Canvas?) {
                c?.drawColor(0, PorterDuff.Mode.CLEAR)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private class RightRoundedRectBackground : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
        }
        private val rect = RectF()
        private val path = Path()

        fun draw(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int, radius: Float) {
            path.reset()
            rect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

            path.moveTo(left.toFloat(), top.toFloat())
            path.lineTo(right.toFloat() - radius, top.toFloat())
            path.quadTo(right.toFloat(), top.toFloat(), right.toFloat(), top.toFloat() + radius)
            path.lineTo(right.toFloat(), bottom.toFloat() - radius)
            path.quadTo(right.toFloat(), bottom.toFloat(), right.toFloat() - radius, bottom.toFloat())
            path.lineTo(left.toFloat(), bottom.toFloat())
            path.close()

            canvas.drawPath(path, paint)
        }

        override fun draw(canvas: Canvas) {}
        override fun setAlpha(alpha: Int) { paint.alpha = alpha }
        override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}