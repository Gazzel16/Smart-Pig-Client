package com.client.smartpigclient.Pigs.Api

import android.content.Context
import com.client.smartpigclient.Config.ApiConfig
import com.client.smartpigclient.Pigs.Model.PigAnalyticsResponse
import com.client.smartpigclient.Pigs.Model.PigBuyerNameRequest
import com.client.smartpigclient.Pigs.Model.PigRequestModel
import com.client.smartpigclient.Pigs.Model.PigsModel
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface AddPigsApi {

    @Multipart
    @POST("/api/pigs/")
    suspend fun addPigs(
        @Part("name") name: RequestBody,
        @Part("breed") breed: RequestBody? = null,
        @Part("age") age: RequestBody? = null,
        @Part("price") price: RequestBody? = null,
        @Part("illness") illness: RequestBody? = null,
        @Part("vaccine") vaccine: RequestBody? = null,

        // ⭐ Added fields
        @Part("vaccineDate") vaccineDate: RequestBody? = null,
        @Part("vaccineNextDue") vaccineNextDue: RequestBody? = null,
        @Part("healthStatus") healthStatus: RequestBody? = null,
        @Part("lastCheckup") lastCheckup: RequestBody? = null,
        @Part("isAlive") isAlive: RequestBody? = null,
        @Part("origin") origin: RequestBody? = null,
        @Part("buyerName") buyerName: RequestBody? = null,

        @Part("isSold") isSold: RequestBody? = null,
        @Part("cageId") cageId: RequestBody? = null,
        @Part("gender") gender: RequestBody? = null,
        @Part("status") status: RequestBody? = null,
        @Part("birthDate") birthDate: RequestBody? = null,
        @Part("weight") weight: RequestBody? = null,
        @Part image: MultipartBody.Part?
    ): Response<PigRequestModel>
}

interface UpdatePigsApi {

    @Multipart
    @PUT("api/pigs/{pig_id}")
    suspend fun updatePigs(
        @Path("pig_id") pigId: String,  // <- Path parameter
        @Part("name") name: RequestBody,
        @Part("breed") breed: RequestBody? = null,
        @Part("age") age: RequestBody? = null,
        @Part("price") price: RequestBody? = null,
        @Part("illness") illness: RequestBody? = null,
        @Part("vaccine") vaccine: RequestBody? = null,
        @Part("vaccineDate") vaccineDate: RequestBody? = null,
        @Part("vaccineNextDue") vaccineNextDue: RequestBody? = null,
        @Part("healthStatus") healthStatus: RequestBody? = null,
        @Part("lastCheckup") lastCheckup: RequestBody? = null,
        @Part("isAlive") isAlive: RequestBody? = null,
        @Part("origin") origin: RequestBody? = null,
        @Part("buyerName") buyerName: RequestBody? = null,
        @Part("isSold") isSold: RequestBody? = null,
        @Part("cageId") cageId: RequestBody? = null,
        @Part("gender") gender: RequestBody? = null,
        @Part("status") status: RequestBody? = null,
        @Part("birthDate") birthDate: RequestBody? = null,
        @Part("weight") weight: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): Response<PigRequestModel>
}

interface PigBuyerNameApi {
    @PATCH("api/pigs/{pig_id}/buyer_name")
    suspend fun pigBuyerName(
        @Path("pig_id") pigId: String,
        @Body data: PigBuyerNameRequest
    ): Response<PigsModel>
}

interface FetchPigsApi {

    @GET("api/pigs/cage/{cage_id}")
    suspend fun fetchPigs(
        @Path("cage_id") cageId: String
    ): List<PigsModel>
}

interface FetchPigsByIdApi {
    @GET("api/pigs/{pig_id}")
    suspend fun fetchPigsById(@Path("pig_id") pigId: String): PigsModel
}

interface GetPigsAnalyticsSummaryApi {
    @GET("/api/pigs/analytics/summary")
    suspend fun getPigsAnalyticsSummary(): PigAnalyticsResponse
}

interface GetPigsAnalyticsSummaryByPeriodApi {
    @GET("/api/pigs/analytics/summary/{year}/{month}")
    suspend fun getPigsAnalyticsSummaryByPeriod(
        @Path("year") year: Int,
        @Path("month") month: Int,
    ): PigAnalyticsResponse
}

object FetchPigsRI {
    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL  // Physical device
        }
    }


    fun getInstance(sharedPrefToken: String): FetchPigsApi {
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
            .create(FetchPigsApi::class.java)
    }
}

object FetchPigsByIdRI {
     val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL // Physical device
        }
    }


    fun getInstance(sharedPrefToken: String): FetchPigsByIdApi {
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
            .create(FetchPigsByIdApi::class.java)
    }
}


object AddPigsRI {
    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL  // Physical device
        }
    }


    fun getInstance(sharedPrefToken: String): AddPigsApi {
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
            .create(AddPigsApi::class.java)
    }
}

object UpdatePigsRI {
    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL // Physical device
        }
    }


    fun getInstance(sharedPrefToken: String): UpdatePigsApi {
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
            .create(UpdatePigsApi::class.java)
    }
}

object PigBuyerNameRI {
    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL // Physical device
        }
    }

    fun getInstance(sharedPrefToken: String): PigBuyerNameApi {
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
            .create(PigBuyerNameApi::class.java)
    }
}

object GetPigsAnalyticsRI {

    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL  // Physical device
        }
    }

    fun getPigsAnalyticsRetrofit(token: String): Retrofit {
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

    // For analytics API
    fun getPigsAnalyticsSummaryApi(token: String): GetPigsAnalyticsSummaryApi {
        return  getPigsAnalyticsRetrofit(token).create(GetPigsAnalyticsSummaryApi::class.java)
    }

    fun getPigsAnalyticsSummaryByPeriodApi(token: String): GetPigsAnalyticsSummaryByPeriodApi{
        return  getPigsAnalyticsRetrofit(token).create(GetPigsAnalyticsSummaryByPeriodApi::class.java)
    }
}
