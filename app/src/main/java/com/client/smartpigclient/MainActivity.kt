package com.client.smartpigclient

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.client.smartpigclient.Cages.Fragments.CagesFragment
import com.client.smartpigclient.Config.FcmTokenConfig
import com.client.smartpigclient.Dashboard.Api.PushNotificationRI
import com.client.smartpigclient.Dashboard.Fragments.DashBoardFragment
import com.client.smartpigclient.Dashboard.Model.TriggerResponse
import com.client.smartpigclient.Pigs.Fragments.PigFragment
import com.client.smartpigclient.Settings.Fragments.SettingsFragment
import com.client.smartpigclient.databinding.ActivityMainBinding
import com.github.mikephil.charting.data.Entry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.util.Locale

import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private val database = FirebaseDatabase.getInstance()
    private val sensorRef = database.getReference("sensor")

    private var ttsReady = false
    private var lastSpokenMessage: String? = null

    private var lastTemperature: Int? = null
    private var lastHumidity: Int? = null


    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Show DashboardFragment first
        replaceFragment(DashBoardFragment())

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    replaceFragment(DashBoardFragment())
                    true
                }
                R.id.nav_cages -> {
                    replaceFragment(CagesFragment())
                    true
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setSpeechRate(0.9f)
                tts.setPitch(1.0f)
                ttsReady = true

                    tempHumidDescription()

                // 🔊 DEBUG TEST (you SHOULD hear this)
                tts.speak("Voice system initialized", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }

        FcmTokenConfig.fetchFcmToken()
    }

    private fun speak(text: String) {
        if (!ttsReady) return

        if (lastSpokenMessage != text) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            lastSpokenMessage = text
        }
    }


    private fun tempHumidDescription() {
        sensorRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.child("temp").getValue(Double::class.java)?.toFloat()
                val humid = snapshot.child("humid").getValue(Double::class.java)?.toFloat()

                if (temp != null && humid != null) {

                    val temperature = temp.toInt()
                    val humidity = humid.toInt()

                    if (temperature != lastTemperature || humidity != lastHumidity){
                        val tempMsg = when {
                            temp < 33 -> "The current temperature is ${temperature}°C. Hot environment. Increase airflow, provide shade, and watch for signs of heat stress."
                            else -> "The current temperature is ${temperature}°C. Extreme heat! Immediate cooling is required—use misting, fans, and limit pig activity."
                        }

                        val humidMsg = when {
                            humid <= 90 -> "The current humidity is ${humidity}% High humidity detected. Increase airflow and keep bedding dry to avoid disease risk."
                            else -> "Extreme humidity. Urgent action needed—maximize ventilation and reduce moisture sources."
                        }

                        val finalMsg = "$tempMsg $humidMsg"

                        speak(finalMsg)
                        lastTemperature = temperature
                        lastHumidity = humidity

                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Optional: log error
                Log.e("Firebase", "Failed to read sensor data", error.toException())
            }
        })
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}
