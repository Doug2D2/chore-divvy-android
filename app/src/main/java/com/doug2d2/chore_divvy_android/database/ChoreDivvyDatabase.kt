package com.doug2d2.chore_divvy_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Category::class], version = 1, exportSchema = false)
abstract class ChoreDivvyDatabase: RoomDatabase() {
    abstract val userDao: UserDao

    companion object {
        @Volatile
        private var INSTANCE: ChoreDivvyDatabase? = null

        fun getDatabase(context: Context): ChoreDivvyDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ChoreDivvyDatabase::class.java,
                        "chore_divvy").fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}