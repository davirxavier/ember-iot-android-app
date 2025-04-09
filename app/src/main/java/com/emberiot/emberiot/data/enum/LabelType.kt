package com.emberiot.emberiot.data.enum

import com.emberiot.emberiot.R

enum class LabelType(val value: String, private val labelId: Int) : EnumFromValue<String, LabelType> {
    NONE("", R.string.none),
    TOP_START("ts", R.string.label_top_start),
    TOP_END("te", R.string.label_top_end),
    TOP_CENTER("tc", R.string.label_top_center),
    BOTTOM_START("bs", R.string.label_bottom_start),
    BOTTOM_END("be", R.string.label_bottom_end),
    BOTTOM_CENTER("bc", R.string.label_bottom_center);

    fun isTop(): Boolean {
        return this == TOP_START || this == TOP_END || this == TOP_CENTER
    }

    override fun getValueInternal(): String {
        return value
    }

    override fun getLabelId(): Int {
        return labelId
    }

    override fun getValues(): List<LabelType> {
        return entries
    }

    override fun getDefault(): EnumFromValue<String, LabelType> {
        return NONE
    }

    companion object {
        fun makeLabel(label: String?, type: LabelType?): String {
            return "${type ?: NONE}${label ?: ""}"
        }
    }
}