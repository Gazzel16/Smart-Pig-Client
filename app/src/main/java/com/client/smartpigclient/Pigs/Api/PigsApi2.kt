package com.client.smartpigclient.Pigs.Api

import com.client.smartpigclient.Config.ApiConfig
import com.client.smartpigclient.Pigs.Model.PigFeedingSchedule
import com.client.smartpigclient.Pigs.Model.PigHistoryResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// -------------------- Retrofit Interface --------------------
interface PigHealthHistoryApi {
    @GET("/api/pigs/health-history/{pig_id}")
    suspend fun getPigHealthHistory(
        @Path("pig_id") pigId: String
    ): PigHistoryResponse
}

interface PigFeedingScheduleApi {
    @GET("/api/feeding_schedule/reference")
    suspend fun getFeedingSchedules(): List<PigFeedingSchedule>
}
// -------------------- Retrofit Instance --------------------
object PigsRI {

    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL  // Physical device
        }
    }

    private fun getRetrofit(token: String): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                chain.proceed(requestBuilder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Correct way to get your API interface
    fun getPigHealthHistoryApi(token: String): PigHealthHistoryApi {
        return getRetrofit(token).create(PigHealthHistoryApi::class.java)
    }

    fun getPigFeedingSchedule(token:String): PigFeedingScheduleApi{
        return getRetrofit(token).create(PigFeedingScheduleApi::class.java)
    }
}
