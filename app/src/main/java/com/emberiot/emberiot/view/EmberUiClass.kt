package com.emberiot.emberiot.view

typealias UpdateChannelFn = (newValue: String) -> Unit

interface EmberUiClass {
    fun parseParams(params: Map<String, String>)
    fun onChannelUpdate(newValue: String)
    fun setOnChannelChangeListener(fn: UpdateChannelFn)
}