package com.doug2d2.chore_divvy_android.network

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.Frequency
import com.doug2d2.chore_divvy_android.database.User
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Json
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

// Contains all API routes for Chore Divvy
interface ChoreDivvyService {
    @POST("login")
    fun login(@Body body: LoginRequest): Deferred<User>

    @POST("sign-up")
    fun signUp(@Body body: SignUpRequest): Deferred<User>

    @PUT("forgot-password")
    fun forgotPassword(@Body body: ForgotPasswordRequest): Deferred<ForgotPasswordResponse>

    @GET("get-chores-by-categoryId/{categoryId}")
    fun getChoresByCategoryId(@Path("categoryId") categoryId: Int): Deferred<List<Chore>>

    @POST("add-chore")
    fun addChore(@Body body: AddChoreRequest): Deferred<Chore>

    @PUT("update-chore/{id}")
    fun updateChore(@Path("id") id: Int, @Body body: UpdateChoreRequest): Deferred<UpdateChoreResponse>

    @GET("get-frequencies")
    fun getFrequencies(): Deferred<List<Frequency>>

    @GET("get-categories-by-userId/{userId}")
    fun getCategoriesByUserId(@Path("userId") userId: Int): Deferred<List<Category>>

    @DELETE("delete-chore/{id}")
    fun deleteChore(@Path("id") id: Int): Deferred<Int>

    @POST("add-category")
    fun addCategory(@Body body: AddCategoryRequest): Deferred<Category>
}

// Sets up retrofit API client
object ChoreDivvyNetwork {
    private val retrofit = Retrofit.Builder()
        //.baseUrl("http://10.0.2.2:8080/") //localhost
        .baseUrl("http://54.208.32.213:8080/")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val choreDivvy = retrofit.create(ChoreDivvyService::class.java)
}

data class LoginRequest(var username: String, var password: String)

data class SignUpRequest(var firstName: String, var lastName: String, var username: String,
                         var password: String)

data class ForgotPasswordRequest(var username: String)

data class ForgotPasswordResponse(var msg: String)

data class UpdateChoreResponse(var numChanged: List<Int>)

data class AddChoreRequest (var choreName: String, var status: String, var frequencyId: Int,
                            var categoryId: Int, var difficulty: String, var notes: String?)

data class UpdateChoreRequest (var id: Int, var choreName: String, var status: String,
                               var dateComplete: String?, var frequencyId: Int, var categoryId: Int,
                               var assigneeId: Int?, var difficulty: String, var notes: String?,
                               var createdAt: String, var updatedAt: String)

data class AddCategoryRequest (var categoryName: String, var userIds: List<Int>)
