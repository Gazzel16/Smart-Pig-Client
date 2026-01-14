package com.client.smartpigclient.Authentication.Api

import com.client.smartpigclient.Authentication.Model.LoginRequest
import com.client.smartpigclient.Authentication.Model.LoginResponse
import com.client.smartpigclient.Authentication.Model.SignupRequest
import com.client.smartpigclient.Authentication.Model.SignupResponse
import com.client.smartpigclient.Authentication.Model.User
import com.client.smartpigclient.Config.ApiConfig
import com.client.smartpigclient.Pigs.Api.GetPigsAnalyticsRI
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/authentication/signup")
    suspend fun signup(
        @Body payload: SignupRequest
    ): SignupResponse

    @POST("/api/authentication/login")
    suspend fun login(
        @Body payload: LoginRequest
    ): LoginResponse
}

object AuthenticationRI {

    private val BASE_URL: String by lazy {
        if (
            android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/"   // Emulator
        } else {
            ApiConfig.BASE_URL       // Physical device
        }
    }

    private val retrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder().build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun authApi(): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
}
