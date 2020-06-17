package com.doug2d2.chore_divvy_android.repository

import com.doug2d2.chore_divvy_android.database.Frequency
import com.doug2d2.chore_divvy_android.database.FrequencyDao
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FrequencyRepository(private val dataSource: FrequencyDao) {
    suspend fun getFrequencies(): List<Frequency> {
        return withContext(Dispatchers.IO) {
            refreshFrequencies()
            dataSource.getAll()
        }
    }

    suspend fun refreshFrequencies() {
        withContext(Dispatchers.IO) {
            val frequencies = ChoreDivvyNetwork.choreDivvy.getFrequencies().await()
            dataSource.deleteAll()
            dataSource.insertAll(frequencies)
        }
    }
}