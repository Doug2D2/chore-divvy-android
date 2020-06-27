package com.doug2d2.chore_divvy_android

import android.content.Context
import android.content.SharedPreferences
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity

// Utils contains common functions used by multiple fragments
object Utils {
    // hideKeyboard hides the on screen keyboard
    fun hideKeyboard(activity: FragmentActivity?) {
        val v = activity!!.window.currentFocus
        if (v != null) {
            val imm =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    // login uses Shared Prefs to hold userID and sets loggedIn to true
    fun login(ctx: Context, userId: Int) {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        editor.putBoolean("loggedIn", true)
        editor.putInt("userID", userId)
        editor.apply()
    }

    // logout removes loggedIn and userID from Shared Prefs
    fun logout(ctx: Context) {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        editor.remove("loggedIn")
        editor.remove("userID")
        editor.apply()
    }

    // isLoggedIn returns true if a user is logged in, false if not
    fun isLoggedIn(ctx: Context): Boolean {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        if (sharedPrefs.getBoolean("loggedIn", false) &&
            sharedPrefs.getInt("userID", -1) != -1)  {
            return true
        }

        return false
    }
}
