package com.example.falai

import kotlinx.serialization.Serializable

@Serializable
data class FalQueueResponse(
    val status: String,
    val request_id: String,
    val response_url: String,
    val status_url: String
)