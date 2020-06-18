package com.doug2d2.chore_divvy_android

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity

object Utils {
    fun hideKeyboard(activity: FragmentActivity?) {
        // Hide keyboard
        val v = activity!!.window.currentFocus
        if (v != null) {
            val imm =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}