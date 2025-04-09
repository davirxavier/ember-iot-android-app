package com.emberiot.emberiot.view

typealias UpdateChannelFn = (newValue: String) -> Unit

interface EmberUiClass {
    fun parseParams(params: Map<String, String>, possibleValues: List<String>? = null)
    fun onChannelUpdate(newValue: String)
    fun setOnChannelChangeListener(fn: UpdateChannelFn)
    fun enableTouch() {}
    fun disableTouch() {}
    fun setWidthAll(px: Int){}
}