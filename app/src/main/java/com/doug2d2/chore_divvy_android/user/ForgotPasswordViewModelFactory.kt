package com.doug2d2.chore_divvy_android.user

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class ForgotPasswordViewModelFactory(val application: Application): ViewModelProvider.Factory {
    @Suppress("unchecked cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            return ForgotPasswordViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
