package com.emberiot.emberiot.data

import com.emberiot.emberiot.data.enum.PropertyType

data class DevicePropertyDefinition(
    val id: String,
    val name: String,
    val type: PropertyType,
    val possibleValues: List<String>
) {
    companion object {
        fun getId(index: Int): String {
            return "CH$index"
        }
    }
}
