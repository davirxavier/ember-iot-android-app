package com.emberiot.emberiot.data

import com.emberiot.emberiot.util.Values

data class Device(
    val id: String,
    val name: String,
    val uiTemplate: String,
    val lastSeen: Long,
    val properties: Map<String, String> = emptyMap(),
) {
    fun isOnline(): Boolean {
        return if (lastSeen < 0) false else System.currentTimeMillis() - lastSeen < Values.OFFLINE_THRESHOLD
    }
}
