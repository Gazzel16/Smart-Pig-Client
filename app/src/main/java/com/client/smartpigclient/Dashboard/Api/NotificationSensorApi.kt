package com.client.smartpigclient.Dashboard.Api

import com.client.smartpigclient.Config.ApiConfig
import com.client.smartpigclient.Dashboard.Model.TriggerResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface TriggerSensorApi {

    @GET("/api/environment_alert_notif/trigger")
    fun triggerSensor(): Call<TriggerResponse>
}

interface ScheduleSensorApi {

    @GET("/api/environment_alert_notif/schedule")
    fun scheduleSensor(): Call<TriggerResponse>
}

object PushNotificationRI {

    private val BASE_URL: String by lazy {
        if (android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator")
        ) {
            "http://10.0.2.2:8000/" // Emulator
        } else {
            ApiConfig.BASE_URL  // Physical device
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


    // For analytics API
    fun triggerSensor(): TriggerSensorApi {
        return retrofit.create(TriggerSensorApi::class.java)
    }

    fun scheduleSensor(): ScheduleSensorApi{
        return retrofit.create(ScheduleSensorApi::class.java)
    }
}