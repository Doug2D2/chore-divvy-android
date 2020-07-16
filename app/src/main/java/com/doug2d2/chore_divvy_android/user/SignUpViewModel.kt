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
import retrofit2.HttpException
import timber.log.Timber

enum class SignUpStatus { LOADING, SUCCESS, USERNAME_INVALID_FORMAT, USERNAME_ALREADY_EXISTS,
    PASSWORD_TOO_SHORT, CONNECTION_ERROR, OTHER_ERROR }

class SignUpViewModel(application: Application): AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val userDao = ChoreDivvyDatabase.getDatabase(application).userDao
    private val userRepository = UserRepository(userDao)

    val firstName = MutableLiveData<String>()
    val lastName = MutableLiveData<String>()
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _signUpStatus = MutableLiveData<SignUpStatus>()
    val signUpStatus: LiveData<SignUpStatus>
        get() = _signUpStatus

    var userID = -1

    // onSignUp is called when the user clicks the Sign Up button
    fun onSignUp() {
        // firstName, lastName, username and password must have a value
        if (!firstName.value.isNullOrBlank() && !lastName.value.isNullOrBlank() &&
            !username.value.isNullOrBlank() && !password.value.isNullOrBlank()) {

            // Username must be a valid email address
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username.value.toString()).matches()) {
                _signUpStatus.value = SignUpStatus.USERNAME_INVALID_FORMAT
                return
            }

            // Password must be 8 characters or more
            if (password.value.toString().length < 8) {
                _signUpStatus.value = SignUpStatus.PASSWORD_TOO_SHORT
                return
            }

            uiScope.launch {
                try {
                    Timber.i("Signing up")
                    _signUpStatus.value = SignUpStatus.LOADING

                    val user = userRepository.signUp(firstName.value.toString(), lastName.value.toString(),
                        username.value.toString(), password.value.toString())
                    userID = user.id

                    _signUpStatus.value = SignUpStatus.SUCCESS
                } catch (e: HttpException) {
                    when(e.code()) {
                        401 -> _signUpStatus.value = SignUpStatus.USERNAME_ALREADY_EXISTS
                        else -> _signUpStatus.value = SignUpStatus.OTHER_ERROR
                    }
                } catch (e: Exception) {
                    Timber.i(e)
                    _signUpStatus.value = SignUpStatus.CONNECTION_ERROR
                }
            }
        }
    }
}
