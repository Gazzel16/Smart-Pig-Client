package com.client.smartpigclient.Pigs.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PigsModel(
    val id: String,
    val name: String,
    val breed: String? = null,
    val age: Int? = null,
    val price: Double? = null,
    val illness: String? = null,
    val vaccine: String? = null,

    // ⭐ Added fields
    val vaccineDate: String? = null,
    val vaccineNextDue: String? = null,
    val healthStatus: String? = null,
    val lastCheckup: String? = null,
    val isAlive: Boolean? = true,
    val origin: String? = null,
    val buyerName: String? = null,

    val isSold: Boolean? = null,
    val cageId: String? = null,
    val cageName: String? = null,
    val gender: String? = null,
    val status: String? = null,
    val birthDate: String? = null,
    val weight: String? = null,
    val qr_url: String? = null,
    val image_url: String? = null,
    val createdAt: String,
    val updatedAt: String
) : Parcelable

data class PigRequestModel(
    val name: String,
    val breed: String? = null,
    val age: Int? = null,
    val price: Double? = null,
    val illness: String? = null,
    val vaccine: String? = null,

    // ⭐ Added fields
    val vaccineDate: String? = null,
    val vaccineNextDue: String? = null,
    val healthStatus: String? = null,
    val lastCheckup: String? = null,
    val isAlive: Boolean? = true,
    val origin: String? = null,
    val buyerName: String? = null,

    val isSold: Boolean? = false,
    val cageId: String? = null,
    val image_url: String? = null,
    val gender: String? = null,
    val status: String? = null,
    val birthDate: String? = null,
    val weight: String? = null
)
