package com.doug2d2.chore_divvy_android.repository

import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import com.doug2d2.chore_divvy_android.network.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val database: ChoreDivvyDatabase) {
    suspend fun login(username: String, password: String) {
        withContext(Dispatchers.IO) {
            val req = LoginRequest(username, password)
            ChoreDivvyNetwork.choreDivvy.login(req).await()
        }
    }
}