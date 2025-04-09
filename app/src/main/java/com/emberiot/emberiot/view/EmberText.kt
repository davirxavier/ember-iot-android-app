package com.emberiot.emberiot.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

class EmberText(context: Context) : AppCompatTextView(context), EmberUiClass {
    companion object {
        const val SIZE = "s"
        const val UNIT = "u"
        const val PREFIX = "p"

        fun adjustSize(size: Int, view: TextView, hasPadding: Boolean = true): Float {
            val newSize: Float

            if (size <= 1) {
                newSize = 16f
            } else if (size == 2) {
                newSize = 24f
            } else {
                newSize = 32f
            }

            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)

            if (hasPadding) {
                val paddingDp = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    newSize,
                    view.context.resources.displayMetrics
                ).toInt()
                view.setPadding((paddingDp * 1.5).toInt(), paddingDp, (paddingDp * 1.5).toInt(), paddingDp)
            }

            return newSize
        }
    }

    private var unit = ""
    private var prefix = ""

    override fun parseParams(params: Map<String, String>) {
        adjustSize(params[SIZE]?.toInt() ?: 1, this, false)

        unit = params[UNIT] ?: ""
        prefix = params[PREFIX] ?: ""
    }

    @SuppressLint("SetTextI18n")
    override fun onChannelUpdate(newValue: String) {
        text = "${prefix}${newValue}${unit}"
    }

    override fun setOnChannelChangeListener(fn: UpdateChannelFn) {}
}