package com.doug2d2.chore_divvy_android.database

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import java.io.Serializable
import java.sql.Date

@Entity(tableName = "chores")
data class Chore (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "chore_name")
    @field:Json(name = "chore_name")
    var choreName: String = "",

    @ColumnInfo(name = "status")
    var status: String = "",

    @ColumnInfo(name = "date_complete")
    @field:Json(name = "date_complete")
    @Nullable
    var dateComplete: String? = "",

    @ColumnInfo(name = "frequency_id")
    @field:Json(name = "frequency_id")
    var frequencyId: Int = 0,

    @ColumnInfo(name = "category_id")
    @field:Json(name = "category_id")
    var categoryId: Int = 0,

    @ColumnInfo(name = "assignee_id")
    @field:Json(name = "assignee_id")
    var assigneeId: Int? = 0,

    @ColumnInfo(name = "difficulty")
    var difficulty: String = "",

    @ColumnInfo(name = "notes")
    @Nullable
    var notes: String?,

    @ColumnInfo(name = "createdAt")
    var createdAt: String = "",

    @ColumnInfo(name = "updatedAt")
    var updatedAt: String = ""
) : Serializable
