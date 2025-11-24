package com.client.smartpigclient.Pigs.Api

import com.client.smartpigclient.ApiConfig.ApiConfig
import com.client.smartpigclient.Cages.Api.AddCageApi
import com.client.smartpigclient.Pigs.Model.PigRequestModel
import com.client.smartpigclient.Pigs.Model.PigsModel
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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


    fun getInstance(): FetchPigsApi {
        val client = OkHttpClient.Builder().build()

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


    fun getInstance(): FetchPigsByIdApi {
        val client = OkHttpClient.Builder().build()

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


    fun getInstance(): AddPigsApi {
        val client = OkHttpClient.Builder().build()

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


    fun getInstance(): UpdatePigsApi {
        val client = OkHttpClient.Builder().build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UpdatePigsApi::class.java)
    }
}
