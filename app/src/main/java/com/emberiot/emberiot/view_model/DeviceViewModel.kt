package com.emberiot.emberiot.view_model

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.DeviceUiObject
import com.emberiot.emberiot.data.builders.DeviceListBuilder
import com.emberiot.emberiot.util.FirebaseLiveData
import com.emberiot.emberiot.util.Values
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class DeviceViewModel(
    private val loginViewModel: LoginViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val devices = FirebaseLiveData(null, DeviceListBuilder())

    init {
        loginViewModel.currentUser.observe(lifecycleOwner) { user ->
            user?.user?.uid?.let {
                devices.updateQuery(
                    database.reference.child("users").child(it).child("devices")
                )
            }
        }
    }

    suspend fun addOrUpdateDevice(device: Device) {
        devices.query?.ref?.let { path ->
            val newPath = if (device.id == null) path.push() else path.child(device.id!!)

            newPath.child("name").setValue(device.name).await()
            newPath.child("icon_id").setValue(device.iconId).await()
            newPath.child("property_definitions").setValue(
                device.propertyDefinitions
                .map { e -> e }
                .associate {
                    it.key to mapOf(
                        "name" to it.value.name,
                        "type" to it.value.type.value,
                        "possible_values" to it.value.possibleValues
                    )
                }).await()

            if (device.id == null) {
                device.id = newPath.key
            }
        }
    }

    suspend fun updateDeviceUi(deviceId: String, objects: List<DeviceUiObject>) {
        devices.query?.ref?.child(deviceId)?.child("ui_objects")?.let { path ->
            val map = mutableMapOf<String, Any>()

            objects.forEachIndexed { i, o ->
                map.put(
                    i.toString(), mapOf(
                        "type" to o.type.name,
                        "prop_id" to o.propDef?.id,
                        "h_pos" to o.horizontalPosition,
                        "v_pos" to o.verticalPosition,
                        "params" to o.parameters
                    )
                )
            }

            path.setValue(map).await()
        }
    }

    suspend fun updateChannel(deviceId: String, channelId: String, newValue: String) {
        devices.query?.ref?.child(deviceId)?.child("properties")?.updateChildren(
            mapOf(
                channelId to mapOf(
                    "d" to newValue,
                    "w" to Values.DEVICE_TYPE
                )
            )
        )?.await()
    }

    suspend fun deleteDevice(deviceId: String) {
        devices.query?.ref?.child(deviceId)?.removeValue()?.await()
    }

    class DeviceViewModelFactory(
        private val loginViewModel: LoginViewModel,
        private val lifecycleOwner: LifecycleOwner
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DeviceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DeviceViewModel(loginViewModel, lifecycleOwner) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}