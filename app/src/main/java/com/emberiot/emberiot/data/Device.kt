package com.emberiot.emberiot.data

import com.emberiot.emberiot.util.Values

data class Device(
    var id: String? = null,
    var name: String = "",
    val lastSeen: Long = -1,
    var iconId: Int = 0,
    val properties: MutableMap<String, String?> = mutableMapOf(),
    val propertyDefinitions: MutableMap<String, DevicePropertyDefinition> = mutableMapOf(),
    val uiObjects: MutableList<DeviceUiObject> = mutableListOf()
) {
    fun isOnline(): Boolean {
        return if (lastSeen < 0) false else (System.currentTimeMillis()/1000) - lastSeen < Values.OFFLINE_THRESHOLD
    }
}
