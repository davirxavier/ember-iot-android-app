package com.emberiot.emberiot.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.enum.EnumFromValue
import com.emberiot.emberiot.data.enum.LabelSize

class EmberText(context: Context) : AppCompatTextView(context), EmberUiClass {
    companion object {
        const val SIZE = "s"
        const val UNIT = "u"
        const val PREFIX = "p"

        fun adjustSize(size: LabelSize, view: TextView, hasPadding: Boolean = true) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.size)

            if (hasPadding) {
                val paddingDp = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    size.size,
                    view.context.resources.displayMetrics
                ).toInt()
                view.setPadding((paddingDp * 1.5).toInt(), paddingDp, (paddingDp * 1.5).toInt(), paddingDp)
            }
        }
    }

    private var unit = ""
    private var prefix = ""

    override fun parseParams(params: Map<String, String>) {
        adjustSize(EnumFromValue.fromValue(params[SIZE], LabelSize::class.java) ?: LabelSize.SMALL, this, false)

        unit = params[UNIT] ?: ""
        prefix = params[PREFIX] ?: ""
    }

    @SuppressLint("SetTextI18n")
    override fun onChannelUpdate(newValue: String) {
        text = "${prefix} ${newValue} ${unit}"
    }

    override fun setOnChannelChangeListener(fn: UpdateChannelFn) {}
}