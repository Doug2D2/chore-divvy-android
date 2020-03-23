package com.doug2d2.chore_divvy_android.repository

import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val database: ChoreDivvyDatabase) {
    suspend fun login() {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.login().await()
        }
    }
}