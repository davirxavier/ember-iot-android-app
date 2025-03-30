package com.emberiot.emberiot.data.builders

import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.util.FirebaseLiveData
import com.google.firebase.database.DataSnapshot

class DeviceListBuilder : FirebaseLiveData.DataBuilder<List<Device>> {
    companion object {
        private val deviceBuilder by lazy { DeviceBuilder() }
    }

    override fun buildData(dataSnapshot: DataSnapshot): List<Device> {
        val ret: MutableList<Device> = mutableListOf()

        for (ds in dataSnapshot.children) {
            val data = deviceBuilder.buildData(ds) ?: continue
            ret.add(data)
        }

        return ret
    }
}