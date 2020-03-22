package com.doug2d2.chore_divvy_android.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "chores")
data class Chore (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "chore_name")
    var choreName: String = "",

    @ColumnInfo(name = "status")
    var status: String = "",

    @ColumnInfo(name = "date_completed")
    var dateCompleted: Date,

    @ColumnInfo(name = "frequency_id")
    var frequencyId: Int = 0,

    @ColumnInfo(name = "category_id")
    var categoryId: Int = 0,

    @ColumnInfo(name = "assignee_id")
    var assigneeId: Int = 0,

    @ColumnInfo(name = "difficulty")
    var difficulty: Int = 0,

    @ColumnInfo(name = "notes")
    var notes: String = ""
)
