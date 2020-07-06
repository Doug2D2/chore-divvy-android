package com.doug2d2.chore_divvy_android.repository

import android.content.Context
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.CategoryDao
import com.doug2d2.chore_divvy_android.network.AddCategoryRequest
import com.doug2d2.chore_divvy_android.network.AddChoreRequest
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(private val dataSource: CategoryDao) {
    // addCategory calls API to add category and updates local db with new data
    suspend fun addCategory(ctx: Context, category: AddCategoryRequest) {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.addCategory(category)
            refreshCategories(ctx)
        }
    }

    // getCategories calls API to get categories, updates local db with new data,
    // and gets categories from local db
    suspend fun getCategories(ctx: Context): List<Category> {
        return withContext(Dispatchers.IO){
            refreshCategories(ctx)
            dataSource.getAll()
        }
    }

    // refreshCategories calls API to get categories, deletes all categories in local db,
    // and then inserts new data into local db
    private suspend fun refreshCategories(ctx: Context) {
        val userId = Utils.getUserId(ctx)

        withContext(Dispatchers.IO) {
            val categories = ChoreDivvyNetwork.choreDivvy.getCategoriesByUserId(userId).await()
            dataSource.deleteAll()
            dataSource.insertAll(categories)
        }
    }
}