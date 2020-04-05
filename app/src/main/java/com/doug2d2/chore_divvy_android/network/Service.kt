package com.doug2d2.chore_divvy_android.network

import com.doug2d2.chore_divvy_android.database.User
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ChoreDivvyService {
    @POST("login")
    fun login(@Body body: LoginRequest): Deferred<User>

    @POST("sign-up")
    fun signUp(@Body body: SignUpRequest): Deferred<User>

    @POST("forgotPassword")
    fun forgotPassword(@Body body: ForgotPasswordRequest): Deferred<ForgotPasswordResponse>
}

object ChoreDivvyNetwork {
    private val retrofit = Retrofit.Builder()
        //.baseUrl("http://10.0.2.2:8080/")
        .baseUrl("http://54.210.21.73:8080/")
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