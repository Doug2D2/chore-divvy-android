package com.doug2d2.chore_divvy_android.database

import androidx.room.*

@Dao
interface FrequencyDao {
    @Insert
    fun insert(frequency: Frequency)

    @Insert
    @JvmSuppressWildcards
    fun insertAll(frequencies: List<Frequency>)

    @Update
    fun update(frequency: Frequency)

    @Delete
    fun delete(frequency: Frequency)

    @Query("DELETE FROM frequencies")
    fun deleteAll()

    @Query("SELECT * FROM frequencies")
    fun getAll(): List<Frequency>

    @Query("SELECT * FROM frequencies WHERE id = :id")
    fun getById(id: Int): Frequency?
}