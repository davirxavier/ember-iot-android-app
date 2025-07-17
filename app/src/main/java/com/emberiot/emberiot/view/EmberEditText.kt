package com.emberiot.emberiot.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.enum.EditTextType
import com.emberiot.emberiot.data.enum.EnumFromValue
import com.emberiot.emberiot.data.enum.ElementSize
import com.emberiot.emberiot.data.enum.UiObjectParameter
import com.emberiot.emberiot.util.UiUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EmberEditText(context: Context) : FrameLayout(context), EmberUiClass {

    private val layout: TextInputLayout
    private val input: TextInputEditText
    private var updateFn: UpdateChannelFn? = null
    private val handler = Handler(Looper.getMainLooper())
    private var touchDisabled = false

    init {
        inflate(context, R.layout.view_input_layout_text_wrapper, this)
        layout = findViewById(R.id.textInputLayout)
        input = findViewById(R.id.input)
    }

    override fun parseParams(params: Map<String, String>, possibleValues: List<String>?) {
        params[UiObjectParameter.TEXT_SIZE.value]?.let {
            val enum = EnumFromValue.fromValue(it, ElementSize::class.java) ?: ElementSize.SMALL
            setWidthAll(UiUtils.dpToPx(enum.width, resources).toInt())
        }

        params[UiObjectParameter.HINT.value]?.let {
            layout.hint = it
        }

        params[UiObjectParameter.EDIT_TYPE.value]?.let {
            EnumFromValue.fromValue(
                it,
                EditTextType::class.java
            )
        }?.let {
            input.inputType = it.type
        }

        if (params[UiObjectParameter.READ_ONLY.value]?.let { it == UiObjectParameter.READ_ONLY_TRUE } == true) {
            touchDisabled = true
            return
        }

        input.addTextChangedListener {
            if (!input.hasFocus()) {
                return@addTextChangedListener
            }

            handler.postDelayed({
                handler.removeCallbacksAndMessages(null)
                updateFn?.invoke(input.text.toString())
            }, 1000)
        }
    }

    override fun onChannelUpdate(newValue: String) {
        val hasFocus = input.hasFocus()
        if (hasFocus) {
            input.clearFocus()
        }

        val selectionStart = input.selectionStart
        val selectionEnd = input.selectionEnd

        input.setText(newValue)

        if (hasFocus) {
            input.requestFocus()
            input.setSelection(selectionStart, selectionEnd)
        }
    }

    override fun setOnChannelChangeListener(fn: UpdateChannelFn) {
        updateFn = fn
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

    override fun enableTouch() {
        touchDisabled = false
    }

    override fun disableTouch() {
        touchDisabled = true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return touchDisabled
    }
}