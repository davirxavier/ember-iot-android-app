package com.emberiot.emberiot.data.enum

enum class LabelAlignment(val value: String) {
    START("l"), END("r"), CENTER("c");

    companion object {
        fun fromValue(search: String?): LabelAlignment? {
            return LabelAlignment.entries.find { it.value.lowercase() == search?.lowercase()}
        }
    }
}