package com.doug2d2.chore_divvy_android.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.database.User
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import com.doug2d2.chore_divvy_android.network.ForgotPasswordRequest
import com.doug2d2.chore_divvy_android.network.LoginRequest
import com.doug2d2.chore_divvy_android.network.SignUpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception

class UserRepository(private val database: ChoreDivvyDatabase) {
    suspend fun login(username: String, password: String): User {
        return withContext(Dispatchers.IO) {
            val req = LoginRequest(username, password)
            ChoreDivvyNetwork.choreDivvy.login(req).await()
        }
    }

    suspend fun signUp(firstName: String, lastName: String, username: String, password: String): User {
        return withContext(Dispatchers.IO) {
            val req = SignUpRequest(firstName, lastName, username, password)
            ChoreDivvyNetwork.choreDivvy.signUp(req).await()
        }
    }

    suspend fun forgotPassword(username: String) {
        withContext(Dispatchers.IO) {
            val req = ForgotPasswordRequest(username)
            ChoreDivvyNetwork.choreDivvy.forgotPassword(req).await()
        }
    }
}
