package com.emberiot.emberiot.data.enum

import com.emberiot.emberiot.R
import com.emberiot.emberiot.util.DeviceViewUtil
import com.emberiot.emberiot.view.EmberButton
import com.emberiot.emberiot.view.EmberText

enum class UiObjectType(
    val nameLabelId: Int,
    val descLabelId: Int,
    val enumParams: Map<String, EnumFromValue<String, *>?>
) {
    BUTTON(
        R.string.ui_element_button_name,
        R.string.ui_element_button_desc,
        mapOf(
            EmberText.SIZE to LabelSize.entries.first(),
            EmberButton.TEXT_ON to null,
            EmberButton.TEXT_OFF to null,
            EmberButton.STYLE to EmberButtonStyle.entries.first(),
            EmberButton.TYPE to EmberButtonType.entries.first(),
            DeviceViewUtil.LABEL_TYPE_PARAM to LabelType.entries.first()
        )
    ),
    TEXT(
        R.string.ui_element_text_name,
        R.string.ui_element_text_desc,
        mapOf(EmberText.SIZE to LabelSize.entries.first(),
            EmberText.PREFIX to null,
            EmberText.UNIT to null,
            DeviceViewUtil.LABEL_PARAM to LabelType.entries.first())
    ),
    EDIT_TEXT(R.string.ui_element_edit_name, R.string.ui_element_edit_desc, mapOf()),
    SELECT(R.string.ui_element_select_name, R.string.ui_element_select_desc, mapOf()),

    INVALID(R.string.ui_element_text_name, R.string.ui_element_text_desc, mapOf());
}