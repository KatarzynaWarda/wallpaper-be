package com.example


import com.example.falai.editImage
import com.example.pixbay.PixabayClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

fun Application.configureRouting() {

    val json = Json {
        ignoreUnknownKeys = true
    }

    val client = HttpClient(CIO) {

        install(ClientContentNegotiation) {
            json(json)
        }
    }

    install(ContentNegotiation) {
        json(json)
    }

    val pixabayApiKey = System.getenv("PIXBAY_KEY")
        ?: error("PIXBAY_KEY is not configured")

    val pixabayClient = PixabayClient(
        httpClient = client,
        apiKey = pixabayApiKey
    )

    routing {
        get("/photography") {
            val query = call.request
                .queryParameters["q"]
                ?: "nature"

            val imageType = call.request
                .queryParameters["image_type"]
                ?: "photo"

            val response = pixabayClient.getPhotography(
                query = query,
                imageType = imageType
            )

            call.respond(response)
        }

        post("/edit-image") {
            editImage(
                call = call,
                client = client,
            )

        }
    }
}