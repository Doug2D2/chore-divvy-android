package com.doug2d2.chore_divvy_android.database

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.moshi.Json
import timber.log.Timber

@TypeConverters(Category.ListConverter::class)
@Entity(tableName = "categories")
data class Category (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "category_name")
    @field:Json(name = "category_name")
    var categoryName: String = "",

    @ColumnInfo(name = "user_id")
    @field:Json(name = "user_id")
    var userId: List<Int>,

    @ColumnInfo(name = "createdAt")
    var createdAt: String = "",

    @ColumnInfo(name = "updatedAt")
    var updatedAt: String = ""
) {
    class ListConverter {
        @TypeConverter
        fun stringToList(value: String): List<Int> {
            val intListType = object : TypeToken<List<Int>>() {}.type
            return Gson().fromJson(value, intListType)
        }

        @TypeConverter
        fun listToString(list: List<Int>): String {
            val gson = Gson()
            return gson.toJson(list)
        }
    }
}
