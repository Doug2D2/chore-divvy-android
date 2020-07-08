package com.doug2d2.chore_divvy_android.category

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class AddCategoryViewModelFactory(val application: Application): ViewModelProvider.Factory {
    @Suppress("unchecked cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddCategoryViewModel::class.java)) {
            return AddCategoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}