package com.doug2d2.chore_divvy_android

import android.content.Context
import android.content.SharedPreferences
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.doug2d2.chore_divvy_android.user.LoginFragmentDirections
import timber.log.Timber

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

    fun login(ctx: Context, userId: Int) {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        editor.putBoolean("loggedIn", true)
        editor.putInt("userID", userId)
        editor.apply()
    }

    fun logout(ctx: Context) {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        editor.remove("loggedIn")
        editor.remove("userID")
        editor.apply()
    }

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