package com.doug2d2.chore_divvy_android.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChoreDao {
    @Insert
    fun insert(chore: Chore)

    @Insert
    @JvmSuppressWildcards
    fun insertAll(chores: List<Chore>)

    @Update
    fun update(chore: Chore)

    @Delete
    fun delete(chore: Chore)

    @Query("DELETE FROM chores")
    fun deleteAll()

    @Query("SELECT chores.*, users.username, users.first_name, users.last_name, " +
            "frequencies.frequency_name, categories.category_name " +
            "FROM chores " +
            "LEFT JOIN users ON assignee_id = users.id " +
            "LEFT JOIN frequencies ON frequency_id = frequencies.id " +
            "LEFT JOIN categories ON category_id = categories.id")
    fun getAll(): List<FullChore>

    @Query("SELECT * FROM chores where id = :id")
    fun getById(id: Int): Chore?
}
