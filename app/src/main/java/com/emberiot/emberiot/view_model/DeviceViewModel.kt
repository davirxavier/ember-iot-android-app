package com.emberiot.emberiot.view_model

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.builders.DeviceListBuilder
import com.emberiot.emberiot.util.FirebaseLiveData
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class DeviceViewModel(private val loginViewModel: LoginViewModel,
                      private val lifecycleOwner: LifecycleOwner) : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val devices = FirebaseLiveData(null, DeviceListBuilder())

    init {
        loginViewModel.currentUser.observe(lifecycleOwner) { user ->
            devices.updateQuery(database.reference.child("users").child(user.user.uid).child("devices"))
        }
    }

    suspend fun addOrUpdateDevice(device: Device) {
        devices.query?.ref?.let { path ->
            val map = mapOf(
                "name" to device.name,
                "icon_id" to device.iconId,
                "property_definitions" to device.propertyDefinitions
                    .map { e -> e }
                    .associate {
                    it.key to if (it.value == null) null else mapOf(
                        "name" to it.value!!.name,
                        "type" to it.value!!.type.value,
                        "possible_values" to it.value!!.possibleValues
                    )
                }
            )

            val newPath = if (device.id == null) path.push() else path.child(device.id!!)
            newPath.updateChildren(map).await()

            if (device.id == null) {
                device.id = newPath.key
            }
        }
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