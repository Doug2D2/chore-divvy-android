package com.doug2d2.chore_divvy_android.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ChoreDivvyService {
    @POST("login")
    fun login(@Body body: LoginRequest): Deferred<LoginResponse>
}

object ChoreDivvyNetwork {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val choreDivvy = retrofit.create(ChoreDivvyService::class.java)
}

data class LoginRequest(var username: String, var password: String)

data class LoginResponse(var msg: String)