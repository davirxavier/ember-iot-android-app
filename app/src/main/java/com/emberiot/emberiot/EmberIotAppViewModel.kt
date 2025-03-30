package com.emberiot.emberiot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EmberIotAppViewModel : ViewModel() {
    private val _isInitialized = MutableLiveData<Boolean>()
    val isInitialized: LiveData<Boolean> get() = _isInitialized

    fun setAppInitialized() {
        _isInitialized.value = true
    }
}