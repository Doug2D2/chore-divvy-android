package com.doug2d2.chore_divvy_android.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
    @Insert
    fun insert(user: User)

    @Insert
    @JvmSuppressWildcards
    fun insertAll(users: List<User>)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)

    @Query("DELETE FROM users")
    fun deleteAll()

    @Query("SELECT * FROM users")
    fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getById(userId: Int): User?

    @Query("SELECT id FROM users WHERE username = :userEmail")
    fun getIdFromEmail(userEmail: String): Int?

    @Query("SELECT id FROM users WHERE username IN (:userEmails)")
    fun getIdsFromEmails(userEmails: List<String>): List<Int>
}