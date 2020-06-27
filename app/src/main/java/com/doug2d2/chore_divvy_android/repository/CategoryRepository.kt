package com.doug2d2.chore_divvy_android.repository

import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.CategoryDao
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(private val dataSource: CategoryDao) {
    // getCategories calls API to get categories, updates local db with new data,
    // and gets categories from local db
    suspend fun getCategories(): List<Category> {
        return withContext(Dispatchers.IO){
            refreshCategories()
            dataSource.getAll()
        }
    }

    // refreshCategories calls API to get categories, deletes all categories in local db,
    // and then inserts new data into local db
    private suspend fun refreshCategories() {
        withContext(Dispatchers.IO) {
            val categories = ChoreDivvyNetwork.choreDivvy.getCategories().await()
            dataSource.deleteAll()
            dataSource.insertAll(categories)
        }
    }
}