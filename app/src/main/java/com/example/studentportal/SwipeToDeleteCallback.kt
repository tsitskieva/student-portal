package com.example.studentportal

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToDeleteCallback(
    private val adapter: SelectedGroupsAdapter,
    private val context: android.content.Context
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val backgroundColor = Color.parseColor("#F71D4D")
    private val deleteIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_delete)
    private val intrinsicWidth: Int = deleteIcon?.intrinsicWidth ?: 0
    private val intrinsicHeight: Int = deleteIcon?.intrinsicHeight ?: 0
    private val paint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
    }
    private val cornerRadius = context.resources.getDimension(R.dimen.corner_radius)
    private val iconMargin = context.resources.getDimensionPixelSize(R.dimen.icon_margin)
    private var iconRect: Rect? = null
    private var lastActionState = -1
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Оставляем пустым, удаление будем обрабатывать вручную
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 1f

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
        val itemHeight = itemView.height.toFloat()
        val maxOffset = itemView.width / 3f
        val limitedDX = if (dX < -maxOffset) -maxOffset else dX

        // Сохраняем последние координаты касания
        if (isCurrentlyActive) {
            lastTouchX = itemView.right + dX
            lastTouchY = itemView.top + itemHeight / 2
        }

        // Рисуем фон с закруглением только справа
        val background = RectF().apply {
            set(
                itemView.right + limitedDX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
        }

        val path = Path().apply {
            addRoundRect(
                background,
                floatArrayOf(0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0f, 0f),
                Path.Direction.CW
            )
        }
        c.drawPath(path, paint)

        // Позиционируем иконку корзины
        deleteIcon?.let { icon ->
            val iconLeft = itemView.right - iconMargin - intrinsicWidth
            val iconRight = itemView.right - iconMargin
            val iconTop = (itemHeight - intrinsicHeight) / 2 + itemView.top
            val iconBottom = iconTop + intrinsicHeight

            iconRect = Rect(iconLeft, iconTop.toInt(), iconRight, iconBottom.toInt())
            icon.bounds = iconRect!!
            icon.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive)
        lastActionState = actionState
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        if (lastActionState == ItemTouchHelper.ACTION_STATE_SWIPE && iconRect != null) {
            // Проверяем, было ли касание в области иконки
            if (iconRect!!.contains(lastTouchX.toInt(), lastTouchY.toInt())) {
                adapter.getGroupAtPosition(viewHolder.adapterPosition)?.let { group ->
                    adapter.removeGroup(group)
                }
            }
        }

        iconRect = null
    }
}