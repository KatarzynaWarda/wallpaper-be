package com.example.falai

import kotlinx.serialization.Serializable

@Serializable
data class FalResult(
    val images: List<FalImage>,
)

@Serializable
data class FalImage(
    val url: String,
)
