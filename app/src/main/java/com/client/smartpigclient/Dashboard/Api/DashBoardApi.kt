package com.client.smartpigclient.Dashboard.Api

import com.client.smartpigclient.ApiConfig.ApiConfig
import com.client.smartpigclient.Pigs.Api.FetchPigsApi
import com.client.smartpigclient.Pigs.Model.PigsModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface FetchAllPigsApi {
    @GET("api/pigs")
    suspend fun fetchAllPigs(): List<PigsModel>
}

object FetchAllPigsRI {
    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL  // Physical device
        }
    }


    fun getInstance(): FetchAllPigsApi {
        val client = OkHttpClient.Builder().build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FetchAllPigsApi::class.java)
    }
}