package com.example.falai

import kotlinx.serialization.Serializable

@Serializable
data class FalEditPayload(
    val prompt: String,
    val image_urls: List<String>,
)
