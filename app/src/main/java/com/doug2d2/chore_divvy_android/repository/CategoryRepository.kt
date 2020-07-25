package com.doug2d2.chore_divvy_android.repository

import android.content.Context
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.CategoryDao
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.network.AddCategoryRequest
import com.doug2d2.chore_divvy_android.network.ChoreDivvyNetwork
import com.doug2d2.chore_divvy_android.network.UpdateCategoryRequest
import com.doug2d2.chore_divvy_android.network.UpdateChoreRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class CategoryRepository(private val dataSource: CategoryDao) {
    // addCategory calls API to add category and updates local db with new data
    suspend fun addCategory(ctx: Context, category: AddCategoryRequest): Int {
        return withContext(Dispatchers.IO) {
            val newCat = ChoreDivvyNetwork.choreDivvy.addCategory(category)
            refreshCategories(ctx)
            newCat.getCompleted().id
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

    suspend fun getCategoryById(categoryId: Int): Category {
        return withContext(Dispatchers.IO) {
            dataSource.getById(categoryId)
        }
    }

    // updateCategory calls API to update a category and
    // updates local db with new data
    suspend fun updateCategory(ctx: Context, category: Category) {
        val categoryToUpdate = UpdateCategoryRequest(id = category.id,
            categoryName = category.categoryName, userIds = category.userId)

        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.updateCategory(category.id, categoryToUpdate).await()
            refreshCategories(ctx)
        }
    }

    // deleteCategory calls API to delete a category and updates local db with new data
    suspend fun deleteCategory(ctx: Context, categoryId: Int) {
        withContext(Dispatchers.IO) {
            ChoreDivvyNetwork.choreDivvy.deleteCategory(categoryId)
            refreshCategories(ctx)
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