package com.emberiot.emberiot.data.enum

import com.emberiot.emberiot.R

enum class PropertyType(val value: String, val labelId: Int) {
    INT("i", R.string.property_type_int),
    DOUBLE("d", R.string.property_type_double),
    STRING("s", R.string.property_type_string),
    ENUM("e", R.string.property_type_enum);

    companion object {
        fun fromValue(value: String?): PropertyType? {
            return if (value == null) null else entries.find { it.value == value }
        }
    }
}