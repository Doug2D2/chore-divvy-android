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
    // addChore calls API to add chore and updates local db with new data
    suspend fun addChore(chore: AddChoreRequest) {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.addChore(chore)
            refreshChores()
        }
    }

    // getChores calls API to get chores, updates local db with new data,
    // and gets chores from local db
    suspend fun getChores(): List<Chore> {
        return withContext(Dispatchers.IO) {
            refreshChores()
            dataSource.getAll()
        }
    }

    // updateChore calls API to update the status of a chore and
    // updates local db with new data
    suspend fun updateChore(chore: Chore) {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.updateChore(chore.id, chore)
            refreshChores()
        }
    }

    // deleteChore calls API to delete a chore and updates local db with new data
    suspend fun deleteChore(choreId: Int) {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.deleteChore(choreId)
            refreshChores()
        }
    }

    // refreshChores calls API to get chores, deletes all chores in local db,
    // and then inserts new data into local db
    private suspend fun refreshChores() {
        withContext(Dispatchers.IO) {
            val chores = ChoreDivvyNetwork.choreDivvy.getChores().await()
            dataSource.deleteAll()
            dataSource.insertAll(chores)
        }
    }
}