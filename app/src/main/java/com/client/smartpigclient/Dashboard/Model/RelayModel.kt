package com.client.smartpigclient.Dashboard.Model

import com.google.gson.annotations.SerializedName

data class RelayModel(
    @SerializedName("is_on") // JSON key from backend
    val is_on: Boolean
)
