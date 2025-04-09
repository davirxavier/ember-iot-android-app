package com.emberiot.emberiot.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class DottedGridView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var rows = 30
    var cols = 30

    private val dotPaint = Paint().apply {
        color = Color.GRAY
        isAntiAlias = false
    }

    private val dotRadius = dpToPx(1f)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cellWidth = width.toFloat() / cols
        val cellHeight = height.toFloat() / rows

        for (i in 1 until cols) {
            for (j in 1 until rows) {
                val cx = i.toFloat() * cellWidth
                val cy = j.toFloat() * cellHeight
                canvas.drawCircle(cx, cy, dotRadius, dotPaint)
            }
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}