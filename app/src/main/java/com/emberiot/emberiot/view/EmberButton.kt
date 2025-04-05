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
import com.emberiot.emberiot.util.UiUtils
import com.google.android.material.button.MaterialButton

class EmberButton(context: Context) : MaterialButton(context), EmberUiClass {
    companion object {
        const val IS_PUSH = "ip"
        const val TEXT_ON = "to"
        const val TEXT_OFF = "tf"
        const val SIZE = "s"
        const val ICON = "i"
        const val STYLE = "sy"

        const val ON_VAL = 1
        const val OFF_VAL = 0
        const val PUSH_VAL = 2

        enum class Style(val value: String) {
            CIRCLE("c"), ROUND("r"), SQUARE("s");

            companion object {
                fun fromValue(v: String?): Style? {
                    return Style.entries.find { it.value == v }
                }
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var updateChannel: UpdateChannelFn? = null
    private var isToggled = false
    private var style: Style = Style.ROUND
    private var size = 1
    private var textOn = ""
    private var textOff = ""
    private var isPush = false
    private var pushStarted = false

    override fun parseParams(params: Map<String, String>) {
        textOn = params[TEXT_ON] ?: ""
        textOff = params[TEXT_OFF] ?: ""
        isPush = params.containsKey(IS_PUSH)
        style = Style.fromValue(params[STYLE]) ?: Style.ROUND

        val paramSize = params[SIZE]?.toInt() ?: 1
        var fontSize = 0f
        var padding = 0f

        if (paramSize <= 1) {
            fontSize = 16f
            padding = 16f
        } else if (paramSize == 2) {
            fontSize = 24f
            padding = 24f
        } else {
            fontSize = 32f
            padding = 32f
        }
        size = paramSize

        setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)

        val paddingDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            padding,
            context.resources.displayMetrics
        ).toInt()
        setPadding((paddingDp * 1.5).toInt(), paddingDp, (paddingDp * 1.5).toInt(), paddingDp)

        iconSize = (paddingDp * 1.5).toInt()

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
            if (isPush) {
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
            Style.ROUND -> {
                border.shape = GradientDrawable.RECTANGLE
                border.cornerRadius = UiUtils.dpToPx(64f, resources)
            }

            Style.CIRCLE -> {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                border.shape = GradientDrawable.OVAL
                val layoutParams = layoutParams
                layoutParams.width = UiUtils.dpToPx(
                    when (size) {
                        1 -> 82f
                        2 -> 96f
                        3 -> 110f
                        4 -> 130f
                        5 -> 150f
                        else -> 150f
                    }, resources
                ).toInt()
                layoutParams.height = layoutParams.width
                this.layoutParams = layoutParams
                setPadding(0, 0, 0, 0)
            }

            Style.SQUARE -> {
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