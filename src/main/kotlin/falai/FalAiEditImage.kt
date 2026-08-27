package com.example.falai


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.delay
import kotlinx.io.readByteArray
import java.util.Base64


suspend fun editImage(
    call: ApplicationCall,
    client: HttpClient,
) {
    val falKey = System.getenv("FAL_KEY")

    if (falKey.isNullOrBlank()) {
        call.respondText(
            "FAL_KEY is not configured",
            status = HttpStatusCode.InternalServerError
        )
        return
    }

    val multipart = call.receiveMultipart()

    var prompt: String? = null
    var imageBytes: ByteArray? = null
    var imageContentType = ContentType.Image.JPEG

    multipart.forEachPart { part ->

        when (part.name) {

            "prompt" -> {
                if (part is PartData.FormItem) {
                    prompt = part.value
                }
            }

            "image" -> {
                if (part is PartData.FileItem) {

                    imageContentType =
                        part.contentType ?: ContentType.Image.JPEG

                    imageBytes = part.provider()
                        .readRemaining()
                        .readByteArray()
                }
            }
        }

        part.release()
    }

    if (prompt.isNullOrBlank()) {
        call.respondText(
            "Missing prompt",
            status = HttpStatusCode.BadRequest
        )
        return
    }

    if (imageBytes == null) {
        call.respondText(
            "Missing image",
            status = HttpStatusCode.BadRequest
        )
        return
    }

    val imageBase64 = Base64
        .getEncoder()
        .encodeToString(imageBytes)

    val imageDataUri =
        "data:${imageContentType};base64,$imageBase64"

    val payload = FalEditPayload(
        prompt = prompt,
        image_urls = listOf(imageDataUri)
    )

    val response = client.post(
        "https://queue.fal.run/fal-ai/flux-2/klein/4b/edit"
    ) {

        header(
            "Authorization",
            "Key $falKey"
        )

        contentType(ContentType.Application.Json)

        setBody(payload)
    }
    if (!response.status.isSuccess()) {

        val errorBody = response.body<String>()

        call.respondText(
            errorBody,
            status = response.status,
            contentType = ContentType.Application.Json
        )

        return
    }

    val queueResponse = response.body<FalQueueResponse>()
    var currentStatus = queueResponse.status

    var attempts = 0
    val maxAttempts = 120

    while (
        currentStatus == "IN_QUEUE" ||
        currentStatus == "IN_PROGRESS"
    ) {

        if (attempts >= maxAttempts) {

            call.respondText(
                "Generation timeout",
                status = HttpStatusCode.GatewayTimeout
            )

            return
        }

        delay(1000)

        attempts++

        val statusResponse = client.get(
            queueResponse.status_url
        ) {

            header(
                "Authorization",
                "Key $falKey"
            )
        }

        if (!statusResponse.status.isSuccess()) {

            val errorBody =
                statusResponse.body<String>()

            call.respondText(
                errorBody,
                status = statusResponse.status,
                contentType = ContentType.Application.Json
            )

            return
        }

        val status =
            statusResponse.body<FalStatusResponse>()

        currentStatus = status.status
    }

    if (currentStatus != "COMPLETED") {

        call.respondText(
            "Generation failed. Status: $currentStatus",
            status = HttpStatusCode.InternalServerError
        )

        return
    }

    val resultResponse = client.get(
        queueResponse.response_url
    ) {

        header(
            "Authorization",
            "Key $falKey"
        )
    }

    if (!resultResponse.status.isSuccess()) {

        val errorBody =
            resultResponse.body<String>()

        call.respondText(
            errorBody,
            status = resultResponse.status,
            contentType = ContentType.Application.Json
        )

        return
    }

    val result =
        resultResponse.body<FalResult>()

    val imageUrl =
        result.images.firstOrNull()?.url

    if (imageUrl == null) {

        call.respondText(
            "No image returned by fal.ai",
            status = HttpStatusCode.InternalServerError
        )

        return
    }

    call.respond(
        mapOf(
            "imageUrl" to imageUrl
        )
    )
}