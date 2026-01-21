package com.client.smartpigclient.Dashboard.Api

import com.client.smartpigclient.Config.ApiConfig
import com.client.smartpigclient.Dashboard.Model.ChatRequest
import com.client.smartpigclient.Dashboard.Model.ChatResponse
import com.client.smartpigclient.Dashboard.Model.RelayModel
import com.client.smartpigclient.Pigs.Model.PigsModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface DashBoardApi {
    @GET("api/pigs")
    suspend fun fetchAllPigs(): List<PigsModel>

    @POST("api/chat_bot/")
    suspend fun chatBotResponse(@Body request: ChatRequest): ChatResponse

    @GET("/api/relay")
    suspend fun getRelayResponse(): RelayModel

    @PUT("/api/relay")
    suspend fun setRelayRequest(@Body relay: RelayModel): RelayModel



}

object DashBoardRI {
    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL  // Physical device
        }
    }


    fun getInstance(sharedPrefToken: String): DashBoardApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $sharedPrefToken") // attach token
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DashBoardApi::class.java)
    }
}