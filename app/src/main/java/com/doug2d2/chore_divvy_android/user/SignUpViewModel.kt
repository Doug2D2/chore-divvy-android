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

enum class SignUpStatus { LOADING, SUCCESS, INVALID_USERNAME, INVALID_PASSWORD, CONNECTION_ERROR, OTHER_ERROR }

class SignUpViewModel(application: Application): AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val userRepository = UserRepository(getDatabase(application))

    val firstName = MutableLiveData<String>()
    val lastName = MutableLiveData<String>()
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _signUpStatus = MutableLiveData<SignUpStatus>()
    val signUpStatus: LiveData<SignUpStatus>
        get() = _signUpStatus

    fun onSignUp() {
        Timber.i("Signing up, firstName: %s, lastName: %s username: %s; password: %s",
            firstName.value, lastName.value, username.value, password.value)

        if (!firstName.value.isNullOrBlank() && !lastName.value.isNullOrBlank() &&
            !username.value.isNullOrBlank() && !password.value.isNullOrBlank()) {
            uiScope.launch {
                try {
                    Timber.i("Signing up")
                    _signUpStatus.value = SignUpStatus.LOADING

                    userRepository.signUp(firstName.value.toString(), lastName.value.toString(),
                        username.value.toString(), password.value.toString())

                    _signUpStatus.value = SignUpStatus.SUCCESS
                } catch (e: HttpException) {
                    _signUpStatus.value = SignUpStatus.OTHER_ERROR
                } catch (e: Exception) {
                    _signUpStatus.value = SignUpStatus.CONNECTION_ERROR
                }
            }
        }
    }
}
