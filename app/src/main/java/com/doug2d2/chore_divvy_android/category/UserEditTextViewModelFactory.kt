package com.doug2d2.chore_divvy_android.category

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class UserEditTextViewModelFactory(val application: Application): ViewModelProvider.Factory {
    @Suppress("unchecked cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserEditTextViewModel::class.java)) {
            return UserEditTextViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
