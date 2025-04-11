package com.emberiot.emberiot.data.enum

import com.emberiot.emberiot.R

enum class LabelSize(val value: String, val size: Float, private val labelId: Int) : EnumFromValue<String, LabelSize> {
    SMALL("s", 16f, R.string.small),
    MEDIUM("m", 24f, R.string.medium),
    LARGE("l", 32f, R.string.large);

    override fun getValueInternal(): String {
        return value
    }

    override fun getLabelId(): Int {
        return labelId
    }

    override fun getValues(): List<LabelSize> {
        return entries
    }

    override fun getDefault(): EnumFromValue<String, LabelSize> {
        return SMALL
    }
}