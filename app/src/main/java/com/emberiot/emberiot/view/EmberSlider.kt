package com.emberiot.emberiot.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import com.emberiot.emberiot.data.enum.ElementSize
import com.emberiot.emberiot.data.enum.EnumFromValue
import com.emberiot.emberiot.data.enum.UiObjectParameter
import com.emberiot.emberiot.util.UiUtils
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs
import kotlin.math.roundToInt

class EmberSlider(context: Context) : Slider(context), EmberUiClass {

    private var updateFn: UpdateChannelFn? = null
    private var touchDisabled: Boolean = false

    init {
        thumbHeight = UiUtils.dpToPx(28f, resources).toInt()
        labelBehavior = LabelFormatter.LABEL_VISIBLE
    }

    override fun parseParams(params: Map<String, String>, possibleValues: List<String>?) {
        var from = params[UiObjectParameter.SLIDER_FROM.value]?.toIntOrNull() ?: 0
        var to = params[UiObjectParameter.SLIDER_TO.value]?.toIntOrNull() ?: (from + 1)

        if (to < from) {
            to = from + 1
        }

        valueFrom = from.toFloat()
        valueTo = to.toFloat()

        if (value < from) {
            value = from.toFloat()
        }

        params[UiObjectParameter.SLIDER_STEP_SIZE.value]?.toFloatOrNull()?.let {
            val step = it.toInt()
            if (step >= 0 && step < valueTo && isMultipleOfStepSize(valueTo - valueFrom, step.toFloat())) {
                stepSize = it
            } else if (step >= 0) {
                stepSize = findClosestValidIntegerStep(step, valueFrom.toInt(), valueTo.toInt()).toFloat()
            } else {
                stepSize = 1.0f
            }
        }

        params[UiObjectParameter.UNITS.value]?.let {
            setLabelFormatter { value: Float ->
                "${value.toInt()} $it"
            }
        }

        val enum = EnumFromValue.fromValue(
            params[UiObjectParameter.TEXT_SIZE.value],
            ElementSize::class.java
        ) ?: ElementSize.SMALL
        setWidthAll(UiUtils.dpToPx(enum.width, resources).toInt())

        if (params[UiObjectParameter.READ_ONLY.value]?.let { it == UiObjectParameter.READ_ONLY_TRUE } == true) {
            touchDisabled = true
            return
        }

        addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                updateFn?.invoke(value.toString())
            }
        })
    }

    override fun onChannelUpdate(newValue: String) {
        newValue.toFloatOrNull()?.let {
            val newValueInRange = adjustSliderValue(it.toInt(), valueFrom.toInt(), stepSize.toInt())
            value = newValueInRange.toFloat().coerceIn(valueFrom, valueTo)

//            if (value != it) { TODO check if necessary
//                updateFn?.invoke(newValueInRange.coerceIn(valueFrom.toInt(), valueTo.toInt()).toString())
//            }
        }
    }

    override fun setOnChannelChangeListener(fn: UpdateChannelFn) {
        updateFn = fn
    }

    override fun setWidthAll(px: Int) {
        layoutParams = layoutParams.apply {
            width = px
        }
    }

    private fun isMultipleOfStepSize(value: Float, stepSize: Float): Boolean {
        val result = BigDecimal(value.toString())
                .divide(BigDecimal(stepSize.toString()), MathContext.DECIMAL64)
                .toDouble()

        return abs(Math.round(result) - result) < .0001
    }

    private fun findClosestValidIntegerStep(userStep: Int, rangeMin: Int, rangeMax: Int): Int {
        val range = rangeMax - rangeMin
        if (userStep <= 0 || range <= 0) return 1

        val divisors = mutableListOf<Int>()
        for (i in 1..range) {
            if (range % i == 0) {
                divisors.add(i)
            }
        }

        return divisors.minByOrNull { kotlin.math.abs(it - userStep) } ?: 1
    }

    private fun adjustSliderValue(value: Int, valueFrom: Int, stepSize: Int): Int {
        if (stepSize == 0) {
            return valueFrom
        }

        val steps = ((value - valueFrom).toDouble() / stepSize).roundToInt()
        return valueFrom + steps * stepSize
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (touchDisabled) false else super.onTouchEvent(event)
    }
}