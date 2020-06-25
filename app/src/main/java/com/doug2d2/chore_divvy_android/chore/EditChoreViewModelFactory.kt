package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class EditChoreViewModelFactory(val application: Application): ViewModelProvider.Factory {
    @Suppress("unchecked cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditChoreViewModel::class.java)) {
            return EditChoreViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
