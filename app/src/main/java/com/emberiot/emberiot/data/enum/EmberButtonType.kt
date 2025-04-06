package com.emberiot.emberiot.data.enum

import com.emberiot.emberiot.R

enum class EmberButtonType(val value: String, private val labelId: Int) : EnumFromValue<String, EmberButtonType> {
    TOGGLE("t", R.string.type_toggle), PUSH("p", R.string.type_push);

    override fun getValueInternal(): String {
        return value
    }

    override fun getLabelId(): Int {
        return labelId
    }

    override fun getValues(): List<EmberButtonType> {
        return entries
    }

    override fun getDefault(): EnumFromValue<String, EmberButtonType> {
        return TOGGLE
    }
}