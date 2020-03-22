package com.doug2d2.chore_divvy_android.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChoreDao {
    @Insert
    fun insert(chore: Chore)

    @Update
    fun update(chore: Chore)

    @Delete
    fun delete(chore: Chore)

    @Query("SELECT * FROM chores")
    fun getAll(): LiveData<List<Chore>>

    @Query("SELECT * FROM chores where id = :id")
    fun getById(id: Int): Chore?
}