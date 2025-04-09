package com.emberiot.emberiot.data.enum

import com.emberiot.emberiot.R

enum class EmberButtonStyle(val value: String, private val labelId: Int) : EnumFromValue<String, EmberButtonStyle> {
    CIRCLE("c", R.string.style_circle),
    ROUND("r", R.string.style_round),
    SQUARE("s", R.string.style_square);

    override fun getValueInternal(): String {
        return value
    }

    override fun getLabelId(): Int {
        return labelId
    }

    override fun getValues(): List<EmberButtonStyle> {
        return entries
    }

    override fun getDefault(): EnumFromValue<String, EmberButtonStyle> {
        return ROUND
    }
}