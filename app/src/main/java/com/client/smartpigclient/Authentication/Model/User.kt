package com.client.smartpigclient.Authentication.Model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    val id: String,
    val fullname: String,
    val email: String,
    val password: String,
    val createdAt: String,
    val updatedAt: String
): Parcelable

data class SignupRequest(
    val fullname: String,
    val email: String,
    val password: String
)
data class SignupResponse(
    val id: String,
    val fullname: String,
    val email: String,
    @SerializedName("created_at")
    val createdAt: String
)
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: Token
)

data class Token(
    val access_token: String,
    val token_type: String
)

