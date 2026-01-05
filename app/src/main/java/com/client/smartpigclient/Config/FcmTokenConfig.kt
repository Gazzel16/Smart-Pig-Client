package com.client.smartpigclient.Config

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.messaging.FirebaseMessaging

//this is a FcmTokenFile
object FcmTokenConfig {

     fun fetchFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM token
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
                saveTokenToRTDB(token)
                // TODO: send this token to your backend server
            }
    }

    private fun saveTokenToRTDB(token: String) {
        val db = FirebaseDatabase.getInstance()
        val ref = db.getReference("fcm_tokens")

        val data = mapOf(
            "token" to token,
        )

        // Use token as key (replace invalid chars)
        val safeKey = token.replace(".", "_")

        ref.child(safeKey)
            .setValue(data)
            .addOnSuccessListener {
                Log.d("FCM", "Token saved to RTDB")
            }
            .addOnFailureListener {
                Log.e("FCM", "Failed to save token", it)
            }
    }

}
