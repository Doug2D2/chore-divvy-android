package com.doug2d2.chore_divvy_android.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frequency")
data class Frequency (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "frequency_name")
    var frequencyName: String = ""
)
