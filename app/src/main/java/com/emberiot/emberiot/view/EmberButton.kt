package com.emberiot.emberiot.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.emberiot.emberiot.EmberIotApp
import com.emberiot.emberiot.R
import com.google.android.material.button.MaterialButton

class EmberButton(context: Context) : MaterialButton(context), EmberUiClass {
    companion object {
        const val IS_PUSH = "ip"
        const val TEXT = "t"
        const val SIZE = "s"
        const val ICON = "i"
    }

    override fun parseParams(params: Map<String, String>) {
        (params[TEXT] ?: "Button").let { this.text = it }

        val paramSize = params[SIZE]?.toInt() ?: 1
        var fontSize = 0f
        var padding = 0f

        if (paramSize <= 1) {
            fontSize = 16f
            padding = 16f
        }
        else if (paramSize == 2) {
            fontSize = 24f
            padding = 24f
        }
        else {
            fontSize = 32f
            padding = 32f
        }

        setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)

        val paddingDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding, context.resources.displayMetrics).toInt()
        setPadding((paddingDp*1.5).toInt(), paddingDp, (paddingDp*1.5).toInt(), paddingDp)

        iconSize = (paddingDp*1.5).toInt()

        params[ICON]?.let {
            EmberIotApp.iconPack?.getIcon(it.toInt())?.drawable?.let { d -> icon = d }
            iconTint = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_onBackground))
        }
    }

    override fun onChannelUpdate(newValue: String) {
    }


}