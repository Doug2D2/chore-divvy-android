package com.doug2d2.chore_divvy_android.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FrequencyDao {
    @Insert
    fun insert(frequency: Frequency)

    @Update
    fun update(frequency: Frequency)

    @Delete
    fun delete(frequency: Frequency)

    @Query("SELECT * FROM frequency")
    fun getAll(): LiveData<List<Frequency>>

    @Query("SELECT * FROM frequency WHERE id = :id")
    fun getById(id: Int): Frequency?
}