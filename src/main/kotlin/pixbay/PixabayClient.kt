package com.example.pixbay

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class PixabayClient(
    private val httpClient: HttpClient,
    private val apiKey: String,
) {

    suspend fun getPhotography(
        query: String = "nature",
        imageType: String = "photo",
    ): PixabayResponseDto {

        return httpClient.get("https://pixabay.com/api/") {
            parameter("key", apiKey)
            parameter("q", query)
            parameter("image_type", imageType)
        }.body()
    }
}