package com.client.smartpigclient.Pigs.Model

data class PigFeedingSchedule(
    val pig: String,
    val currentTime: String,
    val nextSchedule: String,
    val betweenHours: String,
    val advice: String
)
