package com.client.smartpigclient.Pigs.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PigsModel(
    val id: String,
    var  name: String,
    var  breed: String? = null,
    var  age: Int? = null,
    var  price: Double? = null,
    var  illness: String? = null,
    var  vaccine: String? = null,

    // ⭐ Added fields
    var  vaccineDate: String? = null,
    var  vaccineNextDue: String? = null,
    var  healthStatus: String? = null,
    var  lastCheckup: String? = null,
    var  isAlive: Boolean? = true,
    var  origin: String? = null,
    var  buyerName: String? = null,

    var  isSold: Boolean? = null,
    var  cageId: String? = null,
    var  cageName: String? = null,
    var  gender: String? = null,
    var  status: String? = null,
    var  birthDate: String? = null,
    var  weight: String? = null,
    var  qr_url: String? = null,
    var  image_url: String? = null,
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

data class PigBuyerNameRequest(
    val buyerName: String,
    val isSold: Boolean = true
)

data class PigCountResponse (
    val pigsCount: Int
)