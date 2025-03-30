package com.emberiot.emberiot.view_model

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emberiot.emberiot.data.builders.DeviceListBuilder
import com.emberiot.emberiot.util.FirebaseLiveData
import com.google.firebase.database.FirebaseDatabase

class DeviceViewModel(private val loginViewModel: LoginViewModel,
                      private val lifecycleOwner: LifecycleOwner) : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val devices = FirebaseLiveData(null, DeviceListBuilder())

    init {
        loginViewModel.currentUser.observe(lifecycleOwner) { user ->
            devices.updateQuery(database.reference.child("users").child(user.user.uid).child("devices"))
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