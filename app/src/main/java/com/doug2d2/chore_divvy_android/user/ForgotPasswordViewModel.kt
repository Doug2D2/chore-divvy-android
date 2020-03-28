package com.doug2d2.chore_divvy_android.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

enum class ForgotPasswordStatus { LOADING, SUCCESS, INVALID_USERNAME, CONNECTION_ERROR, OTHER_ERROR }

class ForgotPasswordViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val userRepository = UserRepository(getDatabase(application))

    val username = MutableLiveData<String>()

    private val _forgotPasswordStatus = MutableLiveData<ForgotPasswordStatus>()
    val forgotPasswordStatus: LiveData<ForgotPasswordStatus>
        get() = _forgotPasswordStatus

    fun onForgotPassword() {
        Timber.i("Forgot password, username: %s", username.value)

        if (!username.value.isNullOrBlank()) {
            uiScope.launch {
                try {
                    Timber.i("Forgot password")
                    _forgotPasswordStatus.value = ForgotPasswordStatus.LOADING

                    userRepository.forgotPassword(username.value.toString())

                    _forgotPasswordStatus.value = ForgotPasswordStatus.SUCCESS
                } catch (e: Exception) {
                    _forgotPasswordStatus.value = ForgotPasswordStatus.CONNECTION_ERROR
                }
            }
        }
    }
}