package com.emberiot.emberiot.data.enum

import android.content.Context
import androidx.core.content.ContextCompat
import com.emberiot.emberiot.R

enum class UiObjectParameter(
    val value: String,
    private val labelId: Int,
    val enumMapping: EnumFromValue<String, *>?,
    val sampleValue: String?,
    val sampleValueId: Int?,
    vararg val objectType: UiObjectType
) : EnumFromValue<String, UiObjectParameter> {

    TEXT_SIZE(
        "s",
        R.string.size,
        LabelSize.entries.first(),
        LabelSize.SMALL.value,
        null,
        UiObjectType.TEXT,
        UiObjectType.BUTTON,
        UiObjectType.SELECT,
    ),
    UNITS("u", R.string.unit, null, null, R.string.sample_unit, UiObjectType.TEXT),
    PREFIX("p", R.string.prefix, null, null, R.string.sample_prefix, UiObjectType.TEXT),

    BUTTON_TYPE(
        "t",
        R.string.type,
        EmberButtonType.entries.first(),
        EmberButtonType.TOGGLE.value,
        null,
        UiObjectType.BUTTON
    ),
    TEXT_ON("to", R.string.text_on, null, null, R.string.sample_on, UiObjectType.BUTTON),
    TEXT_OFF("tf", R.string.text_off, null, null, R.string.sample_off, UiObjectType.BUTTON),

    //    ICON("i", R.string.select_device_icon, UiObjectType.BUTTON)
    STYLE(
        "sy",
        R.string.style,
        EmberButtonStyle.entries.first(),
        EmberButtonStyle.ROUND.value,
        null,
        UiObjectType.BUTTON
    ),

    HINT(
        "ht",
        R.string.hint,
        null,
        null,
        R.string.sample_hint,
        UiObjectType.SELECT,
        UiObjectType.TEXT
    ),
    DEFAULT("def", R.string.default_val, null, "0", null, UiObjectType.SELECT),

    LABEL("label", R.string.label_hint, null, null, null, *UiObjectType.entries.toTypedArray()),
    LABEL_POSITION(
        "labelt",
        R.string.label_config,
        LabelType.entries.first(),
        null,
        null,
        *UiObjectType.entries.toTypedArray()
    ),

    INVALID("", R.string.sample, null, null, null, UiObjectType.INVALID);

    override fun getValueInternal(): String {
        return value
    }

    override fun getLabelId(): Int {
        return labelId
    }

    override fun getValues(): List<EnumFromValue<String, UiObjectParameter>> {
        return entries
    }

    override fun getDefault(): EnumFromValue<String, UiObjectParameter> {
        return INVALID
    }

    companion object {
        fun getByType(type: UiObjectType): List<UiObjectParameter> {
            return entries.filter { it.objectType.contains(type) }
        }

        fun getParamsByType(type: UiObjectType, context: Context): Map<String, String> {
            return UiObjectParameter.entries.filter { it.objectType.contains(type) }
                .filter { it.sampleValue != null || it.sampleValueId != null }.associate {
                    it.value to (it.sampleValue ?: ContextCompat.getString(
                        context,
                        it.sampleValueId ?: R.string.sample
                    ))
                }
        }
    }
}