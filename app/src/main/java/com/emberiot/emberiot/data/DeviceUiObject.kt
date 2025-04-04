package com.emberiot.emberiot.data

import com.emberiot.emberiot.data.enum.UiObjectType

data class DeviceUiObject(
    val id: String,
    val propDef: DevicePropertyDefinition,
    val type: UiObjectType,
    val parameters: Map<String, String>,
    val horizontalPosition: Float,
    val verticalPosition: Float
)
