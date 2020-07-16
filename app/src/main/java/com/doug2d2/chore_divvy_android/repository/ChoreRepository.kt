package com.doug2d2.chore_divvy_android.repository

import android.content.Context
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.ChoreDao
import com.doug2d2.chore_divvy_android.database.FullChore
import com.doug2d2.chore_divvy_android.network.AddChoreRequest
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import com.doug2d2.chore_divvy_android.network.UpdateChoreRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ChoreRepository(private val dataSource: ChoreDao) {
    // addChore calls API to add chore and updates local db with new data
    suspend fun addChore(ctx: Context, chore: AddChoreRequest) {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.addChore(chore)
            refreshChores(ctx)
        }
    }

    // getChores calls API to get chores, updates local db with new data,
    // and gets chores from local db
    suspend fun getChores(ctx: Context): List<FullChore> {
        return withContext(Dispatchers.IO) {
            refreshChores(ctx)
            dataSource.getAll()
        }
    }

    // updateChore calls API to update the status of a chore and
    // updates local db with new data
    suspend fun updateChore(ctx: Context, chore: Chore) {
        val choreToUpdate = UpdateChoreRequest(id = chore.id,
            choreName = chore.choreName, status = chore.status,
            dateComplete = chore.dateComplete, frequencyId = chore.frequencyId,
            categoryId = chore.categoryId, assigneeId = chore.assigneeId,
            difficulty = chore.difficulty, notes = chore.notes,
            createdAt = chore.createdAt, updatedAt = chore.updatedAt)

        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.updateChore(chore.id, choreToUpdate)
            refreshChores(ctx)
        }
    }

    // deleteChore calls API to delete a chore and updates local db with new data
    suspend fun deleteChore(ctx: Context, choreId: Int) {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.deleteChore(choreId)
            refreshChores(ctx)
        }
    }

    // refreshChores calls API to get chores, deletes all chores in local db,
    // and then inserts new data into local db
    private suspend fun refreshChores(ctx: Context) {
        val categoryId = Utils.getSelectedCategory(ctx)

        withContext(Dispatchers.IO) {
            val chores = ChoreDivvyNetwork.choreDivvy.getChoresByCategoryId(categoryId).await()
            Timber.i("CHORES " + chores)
            dataSource.deleteAll()
            dataSource.insertAll(chores)
        }
    }
}