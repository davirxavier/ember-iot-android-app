package com.emberiot.emberiot.data.enum

import com.emberiot.emberiot.R

enum class ElementSize(val value: String, val fontSize: Float, val width: Float, private val labelId: Int) : EnumFromValue<String, ElementSize> {
    SMALL("s", 16f, 150f, R.string.small),
    MEDIUM("m", 24f, 220f, R.string.medium),
    LARGE("l", 32f, 320f, R.string.large);

    override fun getValueInternal(): String {
        return value
    }

    override fun getLabelId(): Int {
        return labelId
    }

    override fun getValues(): List<ElementSize> {
        return entries
    }

    override fun getDefault(): EnumFromValue<String, ElementSize> {
        return SMALL
    }
}