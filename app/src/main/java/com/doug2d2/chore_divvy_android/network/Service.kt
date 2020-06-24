package com.doug2d2.chore_divvy_android.network

import androidx.annotation.Nullable
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

interface ChoreDivvyService {
    @POST("login")
    fun login(@Body body: LoginRequest): Deferred<User>

    @POST("sign-up")
    fun signUp(@Body body: SignUpRequest): Deferred<User>

    @PUT("forgot-password")
    fun forgotPassword(@Body body: ForgotPasswordRequest): Deferred<ForgotPasswordResponse>

    @GET("get-chores")
    fun getChores(): Deferred<List<Chore>>

    @POST("add-chore")
    fun addChore(@Body body: AddChoreRequest): Deferred<Chore>

    @PUT("update-chore/{id}")
    fun updateChore(@Path("id") id: Int, @Body body: Chore): Deferred<UpdateChoreResponse>

    @GET("get-frequencies")
    fun getFrequencies(): Deferred<List<Frequency>>

    @GET("get-categories")
    fun getCategories(): Deferred<List<Category>>

    @DELETE("delete-chore/{id}")
    fun deleteChore(@Path("id") id: Int): Deferred<Int>
}

object ChoreDivvyNetwork {
    private val retrofit = Retrofit.Builder()
        //.baseUrl("http://10.0.2.2:8080/") //localhost
        .baseUrl("http://54.237.127.150:8080/")
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
