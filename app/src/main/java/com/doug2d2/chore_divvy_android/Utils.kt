package com.doug2d2.chore_divvy_android

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity
import com.doug2d2.chore_divvy_android.chore.ChoreListViewModel
import com.doug2d2.chore_divvy_android.database.Chore
import kotlinx.coroutines.withContext

enum class AddStatus { LOADING, SUCCESS, CONNECTION_ERROR, OTHER_ERROR }

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

    // login uses Shared Prefs to hold userId and sets loggedIn to true
    fun login(ctx: Context, userId: Int) {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        editor.putBoolean("loggedIn", true)
        editor.putInt("userId", userId)
        editor.apply()
    }

    // logout removes loggedIn and userId from Shared Prefs
    fun logout(ctx: Context) {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        editor.remove("loggedIn")
        editor.remove("userId")
        editor.apply()
    }

    // isLoggedIn returns true if a user is logged in, false if not
    fun isLoggedIn(ctx: Context): Boolean {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        if (sharedPrefs.getBoolean("loggedIn", false) &&
            sharedPrefs.getInt("userId", -1) != -1)  {
            return true
        }

        return false
    }

    // getUserId returns the userId of the logged in user
    fun getUserId(ctx: Context): Int {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)

        return sharedPrefs.getInt("userId", -1)
    }

    // setSelectedCategory sets the category id
    fun setSelectedCategory(ctx: Context, categoryId: Int) {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        editor.putInt("categoryId", categoryId)
        editor.apply()
    }

    // getSelectedCategory returns the selected category id
    fun getSelectedCategory(ctx: Context): Int {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)

        return sharedPrefs.getInt("categoryId", -1)
    }

    // isSelectedCategorySet returns true if selectedCategory is set, false if not
    fun isSelectedCategorySet(ctx: Context): Boolean {
        val sharedPrefs: SharedPreferences = ctx.getSharedPreferences("chore-divvy", Context.MODE_PRIVATE)

        if (sharedPrefs.getInt("categoryId", -1) != -1) {
            return true
        }

        return false
    }
}
