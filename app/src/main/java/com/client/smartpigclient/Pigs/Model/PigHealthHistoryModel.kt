package com.client.smartpigclient.Pigs.Model

import java.time.LocalDateTime

// -------------------- Health Event --------------------
data class PigHealthEvent(
    val illness: String? = null,
    val vaccine: String? = null,
    val vaccineDate: String? = null, // was LocalDateTime?
    val vaccineNextDue: String? = null,
    val lastCheckup: String? = null,
    val healthStatus: String? = null,
    val checkupDate: String? = null, // keep as string
    val notes: String? = null,

    val isAlive: Boolean? = null,
    val origin: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val breed: String? = null,
    val pigType: String? = null,
    val age: Int? = null,
    val weight: String? = null,
    val feed: String? = null,
    val price: Double? = null,
    val status: String? = null,
    val cageId: String? = null,
    val cageName: String? = null,
    val actionAt: String? = null,
)




// -------------------- Pig --------------------
data class PigResponse(
    val id: String,
    val name: String,
    val breed: String? = null,
    val pigType: String? = null,
    val feed: String? = null,
    val healthStatus: String? = null,
    val lastCheckup: String? = null, // change to String for formatDate
    val birthDate: String? = null,
    val age: Int? = null,
    val weight: String? = null,
    val price: Double? = null,
    val actionAt: String? = null,
    val image_url: String? = null
)

// -------------------- Pig History Response --------------------
data class PigHistoryResponse(
    val pig: PigResponse,
    val healthHistory: List<PigHealthEvent> = emptyList()
)
