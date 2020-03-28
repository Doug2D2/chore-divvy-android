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
import java.lang.Exception

enum class LoginStatus { LOADING, SUCCESS, INVALID_CREDENTIALS, CONNECTION_ERROR, OTHER_ERROR }

class LoginViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val userRepository = UserRepository(getDatabase(application))

    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _loginStatus = MutableLiveData<LoginStatus>()
    val loginStatus: LiveData<LoginStatus>
        get() = _loginStatus

    private val _navigateToSignUp = MutableLiveData<Boolean>()
    val navigateToSignUp: LiveData<Boolean>
        get() = _navigateToSignUp

    private val _navigateToForgotPassword = MutableLiveData<Boolean>()
    val navigateToForgotPassword: LiveData<Boolean>
        get() = _navigateToForgotPassword

    fun onLogin() {
        Timber.i("Logging in, username: %s; password: %s", username.value, password.value)

        if (username.value != null && password.value != null) {
            uiScope.launch {
                try {
                    Timber.i("Logging in")
                    _loginStatus.value = LoginStatus.LOADING

                    userRepository.login(username.value.toString(), password.value.toString())

                    _loginStatus.value = LoginStatus.SUCCESS
                } catch (e: Exception) {
                    _loginStatus.value = LoginStatus.CONNECTION_ERROR
                }
            }
        }
    }

    fun onSignUp() {
        _navigateToSignUp.value = true
    }

    fun onForgotPassword() {
        _navigateToForgotPassword.value = true
    }
}
