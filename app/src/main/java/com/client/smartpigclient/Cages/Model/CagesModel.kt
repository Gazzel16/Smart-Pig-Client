package com.client.smartpigclient.Cages.Model

import android.os.Parcelable
import com.client.smartpigclient.Pigs.Model.PigsModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class CageModel(
    val id: String,
    val name: String,
    var pigCount: Int = 0, // Number of pigs in this cage
    val qr_url: String? = null, // optional QR code URL if any
    val pigs: List<PigsModel>
) : Parcelable

//For request to create cage
data class CageRequest(
    val name: String
)