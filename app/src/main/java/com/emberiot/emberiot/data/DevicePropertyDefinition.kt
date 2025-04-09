package com.emberiot.emberiot.data

import com.emberiot.emberiot.data.enum.PropertyType

data class DevicePropertyDefinition(
    val id: String,
    val name: String,
    val type: PropertyType,
    val possibleValues: List<String>
) {
    companion object {
        val INVALID = DevicePropertyDefinition("", "", PropertyType.STRING, emptyList())

        fun getId(index: Int): String {
            return "CH$index"
        }
    }
}
