package com.emberiot.emberiot.view_model

import androidx.lifecycle.ViewModel
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.DeviceUiObject

class UiElementConfigViewModel : ViewModel() {
    var currentlyEdited: DeviceUiObject? = null
    var currentDevice: Device? = null
}