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
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception
import kotlin.concurrent.thread

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

    var userID = -1

    private val _navigateToSignUp = MutableLiveData<Boolean>()
    val navigateToSignUp: LiveData<Boolean>
        get() = _navigateToSignUp

    private val _navigateToForgotPassword = MutableLiveData<Boolean>()
    val navigateToForgotPassword: LiveData<Boolean>
        get() = _navigateToForgotPassword

    // onLogin logs the user in by validating their username and password
    fun onLogin() {
        if (!username.value.isNullOrBlank() && !password.value.isNullOrBlank()) {
            uiScope.launch {
                try {
                    Timber.i("Logging in")
                    _loginStatus.value = LoginStatus.LOADING

                    val user = userRepository.login(username.value.toString(), password.value.toString())
                    userID = user.id

                    _loginStatus.value = LoginStatus.SUCCESS
                } catch (e: HttpException) {
                    when(e.code()) {
                        401 -> _loginStatus.value = LoginStatus.INVALID_CREDENTIALS
                        else -> _loginStatus.value = LoginStatus.OTHER_ERROR
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    _loginStatus.value = LoginStatus.CONNECTION_ERROR
                }
            }
        }
    }

    // onSignup navigates to the sign up fragment
    fun onSignUp() {
        _navigateToSignUp.value = true
    }

    // onNavigatedToSignUp is called after navigating to the sign up fragment
    fun onNavigatedToSignUp() {
        _navigateToSignUp.value = false
    }

    // onForgotPassword navigates to the forgot password fragment
    fun onForgotPassword() {
        _navigateToForgotPassword.value = true
    }

    // onNavigatedToForgotPassword is called after navigating to the forgot password fragment
    fun onNavigatedToForgotPassword() {
        _navigateToForgotPassword.value = false
    }
}
