package com.emberiot.emberiot.view

import android.content.Context
import android.content.res.ColorStateList
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.enum.EnumFromValue
import com.emberiot.emberiot.data.enum.LabelSize
import com.emberiot.emberiot.data.enum.UiObjectParameter
import com.emberiot.emberiot.util.UiUtils
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

class EmberSelect(context: Context) : FrameLayout(context), EmberUiClass {
    private var autoCompleteTextView: MaterialAutoCompleteTextView
    private var layout: TextInputLayout
    private var updateFn: UpdateChannelFn? = null
    private var possibleValues = listOf<String>()
    private var touchDisabled = false

    init {
        inflate(context, R.layout.view_input_layout_wrapper, this)

        layout = findViewById(R.id.textInputLayout)
        autoCompleteTextView = findViewById(R.id.input)

        layout.boxStrokeColor = ContextCompat.getColor(context, R.color.md_theme_onBackground)
        layout.hintTextColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_onBackground))

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            if (position < 0) {
                return@setOnItemClickListener
            }

            updateFn?.invoke(position.toString())
        }

        autoCompleteTextView.isFocusable = false
    }

    override fun parseParams(params: Map<String, String>, possibleValues: List<String>?) {
        params[UiObjectParameter.HINT.value]?.let { layout.hint = it }
        possibleValues?.let { this.possibleValues = possibleValues }
        params[UiObjectParameter.DEFAULT.value]?.toIntOrNull()?.let { possibleValues?.get(it) }
            ?.let { onChannelUpdate(it) }

        params[UiObjectParameter.TEXT_SIZE.value]?.let {
            val enum = EnumFromValue.fromValue(it, LabelSize::class.java) ?: LabelSize.SMALL
            setWidthAll(UiUtils.dpToPx(when (enum) {
                LabelSize.SMALL -> 150f
                LabelSize.MEDIUM -> 220f
                LabelSize.LARGE -> 320f
            }, resources).toInt())
        }

        possibleValues?.let {
            autoCompleteTextView.setAdapter(ArrayAdapter(context, R.layout.simple_list_item, it))
        }
    }

    override fun onChannelUpdate(newValue: String) {
        newValue.toIntOrNull()
            ?.let { autoCompleteTextView.setText(possibleValues.getOrNull(it) ?: "", false) }
    }

    override fun setOnChannelChangeListener(fn: UpdateChannelFn) {
        updateFn = fn
    }

    override fun enableTouch() {
        touchDisabled = false
    }

    override fun disableTouch() {
        touchDisabled = true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return touchDisabled
    }

    override fun setWidthAll(px: Int) {
        layoutParams = layoutParams.apply {
            width = px
        }

        children.forEach { child ->
            child.layoutParams = child.layoutParams.apply {
                width = layoutParams.width
            }
        }
    }
}