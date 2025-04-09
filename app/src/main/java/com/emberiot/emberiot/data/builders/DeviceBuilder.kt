package com.emberiot.emberiot.data.builders

import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.DevicePropertyDefinition
import com.emberiot.emberiot.data.enum.PropertyType
import com.emberiot.emberiot.util.FirebaseLiveData
import com.google.firebase.database.DataSnapshot

class DeviceBuilder : FirebaseLiveData.DataBuilder<Device?> {
    override fun buildData(dataSnapshot: DataSnapshot): Device? {
        val id = dataSnapshot.key ?: return null
        val name = dataSnapshot.child("name").getValue(String::class.java) ?: ""
        val uiTemplate = dataSnapshot.child("ui_template").getValue(String::class.java) ?: ""
        val lastSeen = dataSnapshot.child("last_seen").getValue(Long::class.java) ?: -1
        val iconId = dataSnapshot.child("icon_id").getValue(Int::class.java) ?: 0
        val properties = dataSnapshot.child("properties").children.associate {
            it.key!! to it.getValue(String::class.java)
        }.toMutableMap()
        val propertyDefinitions = dataSnapshot.child("property_definitions").children
            .filter { it.hasChild("type") }
            .associate {
                it.key!! to PropertyType.fromValue(it.child("type").getValue(String::class.java))
                    ?.let { propType ->
                        DevicePropertyDefinition(
                            it.child("name").getValue(String::class.java) ?: "",
                            propType,
                            it.child("possible_values").children.mapNotNull { pv -> pv.getValue(String::class.java) }
                        )
                    }
            }.filter { it.value != null }.toMutableMap()

        return Device(id, name, uiTemplate, lastSeen, iconId, properties, propertyDefinitions)
    }
}