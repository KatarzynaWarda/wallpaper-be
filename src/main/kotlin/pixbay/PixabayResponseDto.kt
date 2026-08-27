package com.example.pixbay

import kotlinx.serialization.Serializable

@Serializable
data class PixabayResponseDto(
    val total: Int,
    val totalHits: Int,
    val hits: List<PhotographyDto>
)

@Serializable
data class PhotographyDto(
    val id: Int,
    val tags: String,
    val previewURL: String,
    val webformatURL: String,
    val largeImageURL: String,
    val user: String,
    val userImageURL: String
)