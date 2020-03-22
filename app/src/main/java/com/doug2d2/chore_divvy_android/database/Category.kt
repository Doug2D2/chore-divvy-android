package com.doug2d2.chore_divvy_android.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "category_name")
    var categoryName: String = "",

    @ColumnInfo(name = "user_id")
    var userId: String = ""
)
