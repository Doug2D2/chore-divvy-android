package com.doug2d2.chore_divvy_android.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val userRepository = UserRepository(getDatabase(application))

    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    fun onLogin() {
        Timber.i("Logging in, username: %s; password: %s", username.value, password.value)

        // Navigate to new fragment


        if (username.value != null && password.value != null) {
            uiScope.launch {
                Timber.i("Logging in")
                userRepository.login(username.value.toString(), password.value.toString())
            }
        } else {
            // Display message for user to enter username and password
        }

    }
}
