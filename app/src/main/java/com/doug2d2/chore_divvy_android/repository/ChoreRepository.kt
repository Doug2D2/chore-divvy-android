package com.doug2d2.chore_divvy_android.repository

import androidx.lifecycle.LiveData
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.ChoreDao
import com.doug2d2.chore_divvy_android.network.AddChoreRequest
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ChoreRepository(private val dataSource: ChoreDao) {
    suspend fun addChore(chore: AddChoreRequest) {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.addChore(chore)
            refreshChores()
        }
    }

    suspend fun getChores(): List<Chore> {
        return withContext(Dispatchers.IO) {
            refreshChores()
            dataSource.getAll()
        }
    }

    suspend fun updateChore(chore: Chore) {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.updateChore(chore.id, chore)
            refreshChores()
        }
    }

    suspend fun deleteChore(choreId: Int) {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.deleteChore(choreId)
            refreshChores()
        }
    }

    suspend fun refreshChores() {
        withContext(Dispatchers.IO) {
            val chores = ChoreDivvyNetwork.choreDivvy.getChores().await()
            dataSource.deleteAll()
            dataSource.insertAll(chores)
        }
    }
}