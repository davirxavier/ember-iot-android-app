package com.emberiot.emberiot.data.enum

import com.emberiot.emberiot.R

enum class UiObjectType(val nameLabelId: Int, val descLabelId: Int) {
    BUTTON(R.string.ui_element_button_name, R.string.ui_element_button_desc),
    TEXT(R.string.ui_element_text_name, R.string.ui_element_text_desc),
    EDIT_TEXT(R.string.ui_element_edit_name, R.string.ui_element_edit_desc),
    SELECT(R.string.ui_element_select_name, R.string.ui_element_select_desc),

    INVALID(R.string.ui_element_text_name, R.string.ui_element_text_desc);
}