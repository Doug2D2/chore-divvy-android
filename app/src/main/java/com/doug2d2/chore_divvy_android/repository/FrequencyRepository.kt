package com.doug2d2.chore_divvy_android.repository

import com.doug2d2.chore_divvy_android.database.Frequency
import com.doug2d2.chore_divvy_android.database.FrequencyDao
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FrequencyRepository(private val dataSource: FrequencyDao) {
    // getFrequencies calls API to get frequencies, updates local db with new data,
    // and gets frequencies from local db
    suspend fun getFrequencies(): List<Frequency> {
        return withContext(Dispatchers.IO) {
            refreshFrequencies()
            dataSource.getAll()
        }
    }

    // refreshFrequencies calls API to get frequencies, deletes all frequencies in local db,
    // and then inserts new data into local db
    private suspend fun refreshFrequencies() {
        withContext(Dispatchers.IO) {
            val frequencies = ChoreDivvyNetwork.choreDivvy.getFrequencies().await()
            dataSource.deleteAll()
            dataSource.insertAll(frequencies)
        }
    }
}