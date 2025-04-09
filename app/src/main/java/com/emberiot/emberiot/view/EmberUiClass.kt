package com.emberiot.emberiot.view

interface EmberUiClass {
    fun parseParams(params: Map<String, String>)
    fun onChannelUpdate(newValue: String);
}