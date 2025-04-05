package com.emberiot.emberiot.util

import android.content.res.Resources
import android.util.TypedValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.emberiot.emberiot.R

object UiUtils {
    fun getCurrentFragment(supportFragmentManager: FragmentManager): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main).let {
            it?.childFragmentManager?.fragments?.get(0)
        }
    }

    fun dpToPx(dp: Float, resources: Resources): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }
}