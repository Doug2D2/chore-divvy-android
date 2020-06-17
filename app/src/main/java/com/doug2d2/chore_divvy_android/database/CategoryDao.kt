package com.doug2d2.chore_divvy_android.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {
    @Insert
    fun insert(category: Category)

    @Insert
    @JvmSuppressWildcards
    fun insertAll(categories: List<Category>)

    @Update
    fun update(category: Category)

    @Delete
    fun delete(category: Category)

    @Query("DELETE FROM categories")
    fun deleteAll()

    @Query("SELECT * FROM categories")
    fun getAll(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getById(id: Int): Category?
}