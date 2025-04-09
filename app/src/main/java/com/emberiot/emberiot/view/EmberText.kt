package com.emberiot.emberiot.view

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView

class EmberText(context: Context) : AppCompatTextView(context), EmberUiClass {
    override fun parseParams(params: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun onChannelUpdate(newValue: String) {
        TODO("Not yet implemented")
    }
}