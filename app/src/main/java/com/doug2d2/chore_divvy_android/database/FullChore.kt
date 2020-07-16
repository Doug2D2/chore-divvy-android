package com.doug2d2.chore_divvy_android.database

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import com.squareup.moshi.Json
import java.io.Serializable

data class FullChore (
    var id: Int = 0,

    @ColumnInfo(name = "chore_name")
    @field:Json(name = "chore_name")
    var choreName: String = "",

    var status: String = "",

    @ColumnInfo(name = "date_complete")
    @field:Json(name = "date_complete")
    @Nullable
    var dateComplete: String? = "",

    @ColumnInfo(name = "frequency_id")
    @field:Json(name = "frequency_id")
    var frequencyId: Int = 0,

    @ColumnInfo(name = "frequency_name")
    @field:Json(name = "frequency_name")
    var frequencyName: String?,

    @ColumnInfo(name = "category_id")
    @field:Json(name = "category_id")
    var categoryId: Int = 0,

    @ColumnInfo(name = "category_name")
    @field:Json(name = "category_name")
    var categoryName: String?,

    @ColumnInfo(name = "assignee_id")
    @field:Json(name = "assignee_id")
    var assigneeId: Int? = 0,

    var username: String?,

    @ColumnInfo(name = "first_name")
    @field:Json(name = "first_name")
    var firstName: String?,

    @ColumnInfo(name = "last_name")
    @field:Json(name = "last_name")
    var lastName: String?,

    var difficulty: String = "",

    var notes: String?,

    var createdAt: String = "",

    var updatedAt: String = ""
): Serializable
