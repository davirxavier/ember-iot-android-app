package com.emberiot.emberiot.data.enum

import android.text.InputType
import com.emberiot.emberiot.R

enum class EditTextType(val value: String, private val labelId: Int, val type: Int) : EnumFromValue<String, EditTextType> {
    TEXT("t", R.string.edit_type_text, InputType.TYPE_CLASS_TEXT),
    NUMBER("n", R.string.edit_type_number, InputType.TYPE_CLASS_NUMBER),
    DATE("d", R.string.edit_type_date, InputType.TYPE_CLASS_DATETIME);

    override fun getValueInternal(): String {
        return value
    }

    override fun getLabelId(): Int {
        return labelId
    }

    override fun getValues(): List<EnumFromValue<String, EditTextType>> {
        return entries
    }

    override fun getDefault(): EnumFromValue<String, EditTextType> {
        return TEXT
    }
}