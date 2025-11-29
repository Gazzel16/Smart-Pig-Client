package com.client.smartpigclient.Dashboard.Model

data class ChatRequest(
    val request: String
)

data class ChatResponse(
    val response: String
)
data class ChatMessage(
    val message: String,
    val isUser: Boolean   // true = user, false = bot
)




