package com.emberiot.emberiot.data

import com.emberiot.emberiot.data.enum.UiObjectType

data class DeviceUiObject(
    val id: String,
    var propDef: DevicePropertyDefinition?,
    val type: UiObjectType,
    val parameters: Map<String, String>,
    var horizontalPosition: Float,
    var verticalPosition: Float
)
