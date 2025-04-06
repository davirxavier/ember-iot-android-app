package com.emberiot.emberiot.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.emberiot.emberiot.EmberIotApp
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.enum.EmberButtonStyle
import com.emberiot.emberiot.data.enum.EmberButtonType
import com.emberiot.emberiot.data.enum.EnumFromValue
import com.emberiot.emberiot.data.enum.LabelSize
import com.emberiot.emberiot.util.UiUtils
import com.google.android.material.button.MaterialButton

class EmberButton(context: Context) : MaterialButton(context), EmberUiClass {
    companion object {
        const val TYPE = "t"
        const val TEXT_ON = "to"
        const val TEXT_OFF = "tf"
        const val ICON = "i"
        const val STYLE = "sy"

        const val ON_VAL = 1
        const val OFF_VAL = 0
        const val PUSH_VAL = 2
    }

    private var updateChannel: UpdateChannelFn? = null
    private var isToggled = false
    private var style: EmberButtonStyle = EmberButtonStyle.ROUND
    private var size = LabelSize.SMALL
    private var textOn = ""
    private var textOff = ""
    private var type = EmberButtonType.TOGGLE
    private var pushStarted = false

    override fun parseParams(params: Map<String, String>) {
        textOn = params[TEXT_ON] ?: ""
        textOff = params[TEXT_OFF] ?: ""
        type = EnumFromValue.fromValue(params[TYPE], EmberButtonType::class.java) ?: EmberButtonType.TOGGLE
        style = EnumFromValue.fromValue(params[STYLE], EmberButtonStyle::class.java) ?: EmberButtonStyle.ROUND

        size = EnumFromValue.fromValue(params[EmberText.SIZE], LabelSize::class.java) ?: LabelSize.SMALL
        EmberText.adjustSize(size, this)
        iconSize = (size.size * 1.5).toInt()

        params[ICON]?.let {
            EmberIotApp.iconPack?.getIcon(it.toInt())?.drawable?.let { d -> icon = d }
            iconTint = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    R.color.md_theme_onBackground
                )
            )
        }

        updateStyle()
        setOnClickListener {
            if (type == EmberButtonType.PUSH) {
                if (pushStarted) {
                    return@setOnClickListener
                }

                isToggled = true
                updateStyle()
                pushStarted = true
                updateChannel?.invoke(ON_VAL.toString())
            } else {
                isToggled = !isToggled
                updateStyle()
                updateChannel?.invoke(if (isToggled) ON_VAL.toString() else OFF_VAL.toString())
            }
        }
    }

    private fun updateStyle() {
        text = if (isToggled) textOn else textOff
        setTextColor(
            ContextCompat.getColor(
                context,
                if (isToggled) R.color.md_theme_onBackground else R.color.md_theme_primary
            )
        )

        val border = GradientDrawable()
        border.setStroke(
            UiUtils.dpToPx(2f, resources).toInt(),
            ContextCompat.getColor(context, R.color.md_theme_primary)
        )
        border.setColor(
            if (isToggled) ContextCompat.getColor(
                context,
                R.color.md_theme_primary
            ) else Color.TRANSPARENT
        )

        when (style) {
            EmberButtonStyle.ROUND -> {
                border.shape = GradientDrawable.RECTANGLE
                border.cornerRadius = UiUtils.dpToPx(64f, resources)
            }

            EmberButtonStyle.CIRCLE -> {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                border.shape = GradientDrawable.OVAL
                val layoutParams = layoutParams
                layoutParams.width = UiUtils.dpToPx(
                    when (size) {
                        LabelSize.SMALL -> 82f
                        LabelSize.MEDIUM -> 96f
                        else -> 110f
                    }, resources
                ).toInt()
                layoutParams.height = layoutParams.width
                this.layoutParams = layoutParams
                setPadding(0, 0, 0, 0)
            }

            EmberButtonStyle.SQUARE -> {
                border.setShape(GradientDrawable.RECTANGLE)
                border.cornerRadius = UiUtils.dpToPx(4f, resources)
            }
        }

        background = border
    }

    override fun onChannelUpdate(newValue: String) {
        val intVal = newValue.toInt()

        if (intVal == ON_VAL) {
            isToggled = true
        } else if (intVal == PUSH_VAL) {
            pushStarted = true
            isToggled = true
        }

        updateStyle()
    }

    override fun setOnChannelChangeListener(fn: UpdateChannelFn) {
        updateChannel = fn
    }
}