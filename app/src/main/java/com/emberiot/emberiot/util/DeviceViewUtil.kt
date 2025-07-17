package com.emberiot.emberiot.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.DeviceUiObject
import com.emberiot.emberiot.data.enum.EnumFromValue
import com.emberiot.emberiot.data.enum.LabelType
import com.emberiot.emberiot.data.enum.UiObjectParameter
import com.emberiot.emberiot.data.enum.UiObjectType
import com.emberiot.emberiot.view.DottedGridView
import com.emberiot.emberiot.view.EmberButton
import com.emberiot.emberiot.view.EmberEditText
import com.emberiot.emberiot.view.EmberSelect
import com.emberiot.emberiot.view.EmberSlider
import com.emberiot.emberiot.view.EmberText
import com.emberiot.emberiot.view.EmberUiClass
import kotlin.math.abs
import kotlin.math.roundToInt

typealias DeviceViewChannelUpdateCallback = (channel: String, value: String) -> Unit
typealias OpenUiConfigFn = (obj: DeviceUiObject) -> Unit

class DeviceViewUtil {

    companion object {
        fun getClass(context: Context, type: UiObjectType): View? {
            return when (type) {
                UiObjectType.BUTTON -> EmberButton(context)
                UiObjectType.TEXT -> EmberText(context)
                UiObjectType.SELECT -> EmberSelect(context)
                UiObjectType.EDIT_TEXT -> EmberEditText(context)
                UiObjectType.SLIDER -> EmberSlider(context)
                else -> null
            }
        }

        fun createLabel(label: String, type: LabelType, layout: ConstraintLayout, obj: View) {
            if (type == LabelType.NONE) {
                return
            }

            val labelObj = TextView(layout.context)
            labelObj.id = View.generateViewId()

            labelObj.text = label

            labelObj.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            (labelObj.layoutParams as? ConstraintLayout.LayoutParams)?.let {
                if (type.isTop()) {
                    it.bottomMargin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        4f,
                        layout.context.resources.displayMetrics
                    ).toInt()
                    it.bottomToTop = obj.id
                } else {
                    it.topMargin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        4f,
                        layout.context.resources.displayMetrics
                    ).toInt()
                    it.topToBottom = obj.id
                }

                when (type) {
                    LabelType.TOP_START -> {
                        it.startToStart = obj.id
                    }

                    LabelType.TOP_CENTER -> {
                        it.startToStart = obj.id
                        it.endToEnd = obj.id
                    }

                    LabelType.TOP_END -> {
                        it.endToEnd = obj.id
                    }

                    LabelType.BOTTOM_START -> {
                        it.startToStart = obj.id
                    }

                    LabelType.BOTTOM_CENTER -> {
                        it.startToStart = obj.id
                        it.endToEnd = obj.id
                    }

                    LabelType.BOTTOM_END -> {
                        it.endToEnd = obj.id
                    }

                    else -> {}
                }
            }

            layout.addView(labelObj)
        }
    }

    private val gridSize = 30
    private val objsByChannel = mutableMapOf<String, MutableList<View>>()
    private var wasNearXCenter = false
    private var wasNearYCenter = false

    @SuppressLint("ClickableViewAccessibility")
    fun init(
        layout: ConstraintLayout,
        device: Device,
        editMode: Boolean = false,
        updateChannelFn: DeviceViewChannelUpdateCallback,
        openObjectConfigFn: OpenUiConfigFn,
    ): DeviceViewChannelUpdateCallback {
        layout.removeAllViews()

        var downTime: Long = -1
        var snapHighlight: View? = null
        var dragging: View? = null
        val constraint = ConstraintSet()
        val horizontalGuide = View(layout.context).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                UiUtils.dpToPx(2f, layout.resources).toInt()
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
            id = View.generateViewId()
            background =
                ContextCompat.getColor(layout.context, R.color.md_theme_onBackground).toDrawable()
            visibility = View.GONE
        }
        val verticalGuide = View(layout.context).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                UiUtils.dpToPx(2f, layout.resources).toInt(),
                ConstraintLayout.LayoutParams.MATCH_PARENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
            id = View.generateViewId()
            background =
                ContextCompat.getColor(layout.context, R.color.md_theme_onBackground).toDrawable()
            visibility = View.GONE
        }

        if (editMode) {
            layout.addView(DottedGridView(layout.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            })
            layout.addView(horizontalGuide)
            layout.addView(verticalGuide)
        }

        device.uiObjects.forEach { o ->
            val obj = getClass(layout.context, o.type)

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

            (obj as? TextView)?.apply {
                setTextColor(
                    ContextCompat.getColor(
                        layout.context,
                        R.color.md_theme_onBackground
                    )
                )
            }
            (obj as? EmberUiClass)?.apply {
                if (editMode) {
                    val sample = ContextCompat.getString(layout.context, R.string.sample)
                    val sampleParams = UiObjectParameter.getParamsByType(o.type, layout.context)
                    parseParams(sampleParams.entries.associate {
                        it.key to (o.parameters[it.key] ?: it.value)
                    }, listOf(sample, sample, sample))
                } else {
                    parseParams(o.parameters, o.propDef?.possibleValues ?: listOf())
                }
            }

            layout.addView(obj)
            o.parameters[UiObjectParameter.LABEL.value]?.let {
                createLabel(it, EnumFromValue.fromValue(o.parameters[UiObjectParameter.LABEL_POSITION.value], LabelType::class.java) ?: LabelType.NONE, layout, obj)
            }

            if (editMode) {
                obj.setOnTouchListener { _, event ->
                    Log.println(Log.INFO, "", "$event")
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            downTime = System.currentTimeMillis()
                            return@setOnTouchListener true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val isTimeoutOver = System.currentTimeMillis() - downTime >= 600

                            if (dragging == null && isTimeoutOver) {
                                dragging = obj
                                obj.bringToFront()
                                obj.alpha = 0.7f

                                snapHighlight = View(layout.context).apply {
                                    layoutParams = ConstraintLayout.LayoutParams(
                                        obj.width,
                                        obj.height
                                    ).apply {
                                        startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                                        endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                                        topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                                        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                                        id = View.generateViewId()
                                    }
                                    setBackgroundColor(
                                        ContextCompat.getColor(
                                            layout.context,
                                            R.color.green
                                        )
                                    )
                                }
                                layout.addView(snapHighlight)
                            }

                            if (dragging != null && isTimeoutOver) {
                                val id = snapHighlight?.id ?: return@setOnTouchListener false
                                val idView = dragging?.id ?: return@setOnTouchListener false
                                val biases = computeBiasFromTouch(event.rawX, event.rawY, layout)

                                val nearCenterX = abs(biases.first - 0.5f) < 0.03
                                val nearCenterY = abs(biases.second - 0.5f) < 0.03

                                verticalGuide.visibility =
                                    if (nearCenterX) View.VISIBLE else View.GONE
                                horizontalGuide.visibility =
                                    if (nearCenterY) View.VISIBLE else View.GONE

                                val finalHBias = if (nearCenterX) 0.5f else biases.first
                                val finalVBias = if (nearCenterY) 0.5f else biases.second

                                constraint.clone(layout)
                                constraint.setHorizontalBias(id, finalHBias)
                                constraint.setVerticalBias(id, finalVBias)
                                constraint.applyTo(layout)

                                constraint.clone(layout)
                                constraint.setHorizontalBias(idView, finalHBias)
                                constraint.setVerticalBias(idView, finalVBias)
                                constraint.applyTo(layout)

                                wasNearXCenter = nearCenterX
                                wasNearYCenter = nearCenterY
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if (dragging == null && System.currentTimeMillis() - downTime < 600) {
                                openObjectConfigFn(o)
                                return@setOnTouchListener true
                            }

                            if (dragging == null) return@setOnTouchListener false

                            (dragging?.layoutParams as? ConstraintLayout.LayoutParams)?.let {
                                o.horizontalPosition = it.horizontalBias
                                o.verticalPosition = it.verticalBias
                            }

                            dragging?.alpha = 1f
                            layout.removeView(snapHighlight)
                            snapHighlight = null
                            dragging = null
                            wasNearXCenter = false
                            wasNearYCenter = false
                            horizontalGuide.visibility = View.GONE
                            verticalGuide.visibility = View.GONE
                        }
                    }
                    true
                }
            }

            o.propDef?.id?.let {
                if (!objsByChannel.containsKey(it)) {
                    objsByChannel[it] = mutableListOf()
                }

                objsByChannel[it]?.add(obj)
            }

            if (obj is EmberUiClass) {
                o.propDef?.id?.let {
                    device.properties[it]?.let { it1 -> obj.onChannelUpdate(it1) }

                    if (!editMode) {
                        obj.setOnChannelChangeListener { newVal ->
                            updateChannelFn(it, newVal)
                        }
                    }
                }

                if (editMode && obj is EmberText) {
                    obj.onChannelUpdate("123")
                }

                if (editMode) {
                    obj.disableTouch()
                }
            }
        }

        return { channel, value ->
            objsByChannel[channel]?.forEach {
                (it as? EmberUiClass)?.onChannelUpdate(value)
            }
        }
    }

    private fun computeBiasFromTouch(
        rawX: Float,
        rawY: Float,
        layout: ConstraintLayout
    ): Pair<Float, Float> {
        val location = IntArray(2)
        layout.getLocationOnScreen(location)
        val layoutX = rawX - location[0]
        val layoutY = rawY - location[1]

        val width = layout.width.toFloat()
        val height = layout.height.toFloat()

        val gridRows = gridSize
        val gridCols = gridSize

        val colWidth = width / gridCols
        val rowHeight = height / gridRows

        val col = (layoutX / colWidth).roundToInt().coerceIn(0, gridCols)
        val row = (layoutY / rowHeight).roundToInt().coerceIn(0, gridRows)

        val hBias = col / gridCols.toFloat()
        val vBias = row / gridRows.toFloat()

        return hBias to vBias
    }
}