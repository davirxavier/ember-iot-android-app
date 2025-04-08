package com.emberiot.emberiot.data.enum

import com.emberiot.emberiot.R

enum class UiObjectType(
    val nameLabelId: Int,
    val descLabelId: Int,
    vararg val compatiblePropertyTypes: PropertyType,
) {
    BUTTON(
        R.string.ui_element_button_name,
        R.string.ui_element_button_desc,
        PropertyType.INT,
    ),
    TEXT(
        R.string.ui_element_text_name,
        R.string.ui_element_text_desc,
        PropertyType.STRING,
        PropertyType.ENUM,
        PropertyType.INT,
        PropertyType.DOUBLE
    ),
    EDIT_TEXT(R.string.ui_element_edit_name, R.string.ui_element_edit_desc, PropertyType.STRING),
    SELECT(R.string.ui_element_select_name, R.string.ui_element_select_desc, PropertyType.ENUM),

    INVALID(R.string.ui_element_text_name, R.string.ui_element_text_desc);
}