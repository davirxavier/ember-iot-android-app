package com.emberiot.emberiot.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.enum.EnumFromValue
import com.emberiot.emberiot.data.enum.LabelSize
import com.emberiot.emberiot.data.enum.UiObjectParameter

class EmberText(context: Context) : AppCompatTextView(context), EmberUiClass {
    companion object {
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
    private var possibleValues: List<String>? = null

    override fun parseParams(params: Map<String, String>, possibleValues: List<String>?) {
        adjustSize(EnumFromValue.fromValue(params[UiObjectParameter.TEXT_SIZE.value], LabelSize::class.java) ?: LabelSize.SMALL, this, false)

        unit = params[UiObjectParameter.UNITS.value] ?: ""
        prefix = params[UiObjectParameter.PREFIX.value] ?: ""

        this.possibleValues = possibleValues
    }

    @SuppressLint("SetTextI18n")
    override fun onChannelUpdate(newValue: String) {
        val valueText = newValue.toIntOrNull()?.let { possibleValues?.getOrNull(it) } ?: newValue
        text = "${prefix} ${valueText} ${unit}"
    }

    override fun setOnChannelChangeListener(fn: UpdateChannelFn) {}
}