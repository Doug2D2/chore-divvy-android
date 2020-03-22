package com.doug2d2.chore_divvy_android.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {
    @Insert
    fun insert(category: Category)

    @Update
    fun update(category: Category)

    @Delete
    fun delete(category: Category)

    @Query("SELECT * FROM categories")
    fun getAll(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getById(id: Int): Category?
}