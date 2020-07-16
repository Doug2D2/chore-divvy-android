package com.doug2d2.chore_divvy_android.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "frequencies")
data class Frequency (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "frequency_name")
    @field:Json(name = "frequency_name")
    var frequencyName: String = "",

    @ColumnInfo(name = "createdAt")
    var createdAt: String = "",

    @ColumnInfo(name = "updatedAt")
    var updatedAt: String = ""
)
