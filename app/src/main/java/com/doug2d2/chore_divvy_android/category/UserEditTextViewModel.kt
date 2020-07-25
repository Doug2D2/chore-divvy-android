package com.doug2d2.chore_divvy_android.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class UserEditTextViewModel(application: Application): AndroidViewModel(application) {
    private val _removeUserEditText = MutableLiveData<Boolean>()
    val removeUserEditText: LiveData<Boolean>
        get() = _removeUserEditText

    val user = MutableLiveData<String>()

    // onRemoveUserEdit is called when the minus button is clicked
    fun onRemoveUserEditText() {
        _removeUserEditText.value = true
    }

    // doneRemoveUserEdit is called when finished removing user edit text
    fun doneRemoveUserEditText() {
        _removeUserEditText.value = false
    }
}