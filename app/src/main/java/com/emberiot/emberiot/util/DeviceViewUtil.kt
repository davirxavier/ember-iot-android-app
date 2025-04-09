package com.emberiot.emberiot.util

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.enum.LabelAlignment
import com.emberiot.emberiot.data.enum.UiObjectType
import com.emberiot.emberiot.view.EmberButton
import com.emberiot.emberiot.view.EmberText
import com.emberiot.emberiot.view.EmberUiClass

typealias DeviceViewChannelUpdateCallback = (channel: String, value: String) -> Unit

class DeviceViewUtil {

    companion object {
        const val LABEL_PARAM = "label"
    }

    private val objByChannel = mutableMapOf<String, View>()

    fun init(
        layout: ConstraintLayout,
        device: Device,
    ): DeviceViewChannelUpdateCallback {
        layout.removeAllViews()

        device.uiObjects.forEach { o ->
            val obj = when (o.type) {
                UiObjectType.BUTTON -> EmberButton(layout.context)
                UiObjectType.TEXT -> EmberText(layout.context)
                else -> null
            }

            if (obj == null) {
                return@forEach
            }

            obj.id = View.generateViewId()

            obj.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            (obj.layoutParams as? ConstraintLayout.LayoutParams)?.let {
                it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

                it.horizontalBias = o.horizontalPosition
                it.verticalBias = o.verticalPosition
            }

            obj.setTextColor(ContextCompat.getColor(layout.context, R.color.md_theme_onBackground))
            obj.parseParams(o.parameters)

            layout.addView(obj)

            o.parameters[LABEL_PARAM]?.let { label ->
                if (label.length < 2) {
                    return@let
                }

                val alignment = LabelAlignment.fromValue(label[0].toString()) ?: LabelAlignment.CENTER
                val labelText = label.substring(1)

                val labelObj = TextView(layout.context)
                labelObj.id = View.generateViewId()

                labelObj.text = labelText

                labelObj.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )

                (labelObj.layoutParams as? ConstraintLayout.LayoutParams)?.let {
                    it.bottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, layout.context.resources.displayMetrics).toInt()
                    it.bottomToTop = obj.id

                    when (alignment) {
                        LabelAlignment.CENTER -> {
                            it.startToStart = obj.id
                            it.endToEnd = obj.id
                        }
                        LabelAlignment.START -> {
                            it.startToStart = obj.id
                        }
                        LabelAlignment.END -> {
                            it.endToEnd = obj.id
                        }
                    }
                }

                layout.addView(labelObj)
            }

            objByChannel[o.propDef.id] = obj
        }

        return { channel, value ->
            (objByChannel[channel] as? EmberUiClass)?.onChannelUpdate(value)
        }
    }

}