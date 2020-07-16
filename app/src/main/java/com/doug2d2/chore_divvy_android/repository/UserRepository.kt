package com.doug2d2.chore_divvy_android.repository

import com.doug2d2.chore_divvy_android.database.*
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import com.doug2d2.chore_divvy_android.network.ForgotPasswordRequest
import com.doug2d2.chore_divvy_android.network.LoginRequest
import com.doug2d2.chore_divvy_android.network.SignUpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val dataSource: UserDao) {
    // login calls API to log user in
    suspend fun login(username: String, password: String): User {
        return withContext(Dispatchers.IO) {
            val req = LoginRequest(username, password)
            ChoreDivvyNetwork.choreDivvy.login(req).await()
        }
    }

    // signUp calls API to create new user
    suspend fun signUp(firstName: String, lastName: String, username: String, password: String): User {
        return withContext(Dispatchers.IO) {
            val req = SignUpRequest(firstName, lastName, username, password)
            ChoreDivvyNetwork.choreDivvy.signUp(req).await()
        }
    }

    // forgotPassword calls API to send user a new password
    suspend fun forgotPassword(username: String) {
        withContext(Dispatchers.IO) {
            val req = ForgotPasswordRequest(username)
            ChoreDivvyNetwork.choreDivvy.forgotPassword(req).await()
        }
    }

    // getUsers calls API to get users, updates local db with new data,
    // and gets users from local db
    suspend fun getUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            refreshUsers()
            dataSource.getAll()
        }
    }

    // refreshUsers calls API to get users, deletes all users in local db,
    // and then inserts new data into local db
    private suspend fun refreshUsers() {
        withContext(Dispatchers.IO) {
            val users = ChoreDivvyNetwork.choreDivvy.getUsers().await()
            dataSource.deleteAll()
            dataSource.insertAll(users)
        }
    }
}
