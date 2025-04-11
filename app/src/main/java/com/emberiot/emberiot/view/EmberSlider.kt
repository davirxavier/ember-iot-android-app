package com.emberiot.emberiot.view

import android.content.Context
import com.emberiot.emberiot.data.enum.ElementSize
import com.emberiot.emberiot.data.enum.EnumFromValue
import com.emberiot.emberiot.data.enum.UiObjectParameter
import com.emberiot.emberiot.util.UiUtils
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider

class EmberSlider(context: Context) : Slider(context), EmberUiClass {

    private var updateFn: UpdateChannelFn? = null

    init {
        thumbHeight = UiUtils.dpToPx(28f, resources).toInt()

        addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                updateFn?.invoke(value.toString())
            }
        })

        labelBehavior = LabelFormatter.LABEL_VISIBLE
    }

    override fun parseParams(params: Map<String, String>, possibleValues: List<String>?) {
        var from = params[UiObjectParameter.SLIDER_FROM.value]?.toFloatOrNull() ?: 0.0f
        var to = params[UiObjectParameter.SLIDER_TO.value]?.toFloatOrNull() ?: (from + 1.0f)

        if (to < from) {
            to = from + 1
        }

        valueFrom = from
        valueTo = to

        if (value < from) {
            value = from
        }

        params[UiObjectParameter.SLIDER_STEP_SIZE.value]?.toFloatOrNull()?.let {
            stepSize = it
        }

        params[UiObjectParameter.UNITS.value]?.let {
            setLabelFormatter { value: Float ->
                "$value $it"
            }
        }

        val enum = EnumFromValue.fromValue(
            params[UiObjectParameter.TEXT_SIZE.value],
            ElementSize::class.java
        ) ?: ElementSize.SMALL
        setWidthAll(UiUtils.dpToPx(enum.width, resources).toInt())
    }

    override fun onChannelUpdate(newValue: String) {
        newValue.toFloatOrNull()?.let { value = it.coerceIn(valueFrom, valueTo) }
    }

    override fun setOnChannelChangeListener(fn: UpdateChannelFn) {
        updateFn = fn
    }

    override fun setWidthAll(px: Int) {
        layoutParams = layoutParams.apply {
            width = px
        }
    }
}