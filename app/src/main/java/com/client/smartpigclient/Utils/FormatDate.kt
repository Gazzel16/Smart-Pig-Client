package com.client.smartpigclient.Utils

import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(dateString: String?): String {
    return if (!dateString.isNullOrEmpty()) {
        try {
            // Input: "2026-01-31T00:00:00"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: "N/A"
        } catch (e: Exception) {
            "N/A"
        }
    } else {
        "N/A"
    }
}

fun formatDateWithoutHours(dateString: String?): String {
    return if (!dateString.isNullOrEmpty()) {
        try {
            // Input: "2026-01-31T00:00:00"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: "N/A"
        } catch (e: Exception) {
            "N/A"
        }
    } else {
        "N/A"
    }
}
