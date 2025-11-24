package com.client.smartpigclient.Cages.Api

import com.client.smartpigclient.ApiConfig.ApiConfig
import com.client.smartpigclient.Cages.Model.CageModel
import com.client.smartpigclient.Cages.Model.CageRequest
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AddCageApi {
    @POST("api/cages/")
    suspend fun addCage(@Body request: CageRequest): CageModel
}

interface FetchCageApi {
    @GET("api/cages/")
    suspend fun fetchCage(): List<CageModel>
}

interface FetchCageByIdApi {
    @GET("api/cages/{cage_id}")
    suspend fun fetchCageById(
        @Path("cage_id") cageId: String
    ): CageModel  // if your backend returns a single object
}
object AddCageRI {
    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL // Physical device
        }
    }


    fun getInstance(): AddCageApi {
        val client = OkHttpClient.Builder().build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AddCageApi::class.java)
    }
}

object FetchCageRI {
     val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL // Physical device
        }
    }


    fun getInstance(): FetchCageApi {
        val client = OkHttpClient.Builder().build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FetchCageApi::class.java)
    }
}

object FetchCageByIdRI {
    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL  // Physical device
        }
    }


    fun getInstance(): FetchCageByIdApi {
        val client = OkHttpClient.Builder().build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FetchCageByIdApi::class.java)
    }
}