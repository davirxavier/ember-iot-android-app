package com.emberiot.emberiot.data.builders

import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.util.FirebaseLiveData
import com.google.firebase.database.DataSnapshot

class DeviceBuilder : FirebaseLiveData.DataBuilder<Device?> {
    override fun buildData(dataSnapshot: DataSnapshot): Device? {
        val id = dataSnapshot.key ?: return null
        val name = dataSnapshot.child("name").getValue(String::class.java) ?: ""
        val uiTemplate = dataSnapshot.child("ui_template").getValue(String::class.java) ?: ""
        val lastSeen = dataSnapshot.child("last_seen").getValue(Long::class.java) ?: -1
        val properties = dataSnapshot.child("properties").children.associate {
            it.key!! to it.getValue(String::class.java)!!
        }

        return Device(id, name, uiTemplate, lastSeen, properties)
    }
}