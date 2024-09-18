package com.schoolkiller.data_Layer.network.api

import com.schoolkiller.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import javax.inject.Inject

class GeminiApiService @Inject constructor(
    private val client: HttpClient
) {

    suspend fun uploadFileWithProgress(
        fileByteArray: ByteArray,
        fileName: String,
//        progressListener: (Long, Long) -> Unit
    ): Result<UploadModel> {

        val response: HttpResponse = client.post(
            "https://generativelanguage.googleapis.com/upload/v1beta/files?key=${BuildConfig.gemini_api_key}"
        ) {
            header("X-Goog-Upload-Protocol", "resumable")
            header("X-Goog-Upload-Command", "start")
            header("X-Goog-Upload-Header-Content-Length", fileByteArray.size)
            header("X-Goog-Upload-Header-Content-Type", "image/jpeg")
            contentType(ContentType.Application.Json)
            setBody("{'file': {'display_name': '$fileName'}}")
        }

        val uploadUrl =
            response.headers["X-Goog-Upload-URL"] ?: throw Exception("Upload URL not found")
        return Result.success(UploadModel(uploadUrl))

    }

    suspend fun uploadFileBytes(
        uploadUrl: String,
        fileByteArray: ByteArray,
//        progressListener: (Long, Long) -> Unit
    ): Result<String> {

        val response: HttpResponse = client.post(uploadUrl) {
            header("Content-Length", fileByteArray.size)
            header("X-Goog-Upload-Offset", 0)
            header("X-Goog-Upload-Command", "upload, finalize")
            setBody(ByteReadChannel(fileByteArray))
        }

        val fileUri = response.bodyAsText()
        return Result.success(fileUri)

    }

    suspend fun generateContent(fileUri: String, prompt: String): String {

        val escapedFileUri = fileUri.replace("\"", "\\\"")

        val response: HttpResponse = client.post(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${BuildConfig.gemini_api_key}") {
            contentType(ContentType.Application.Json)
            setBody("""
            {
                "contents": [{
                    "parts":[
                        {"text": "$prompt"},
                        {"file_data": {"mime_type": "image/jpeg", "file_uri": "$escapedFileUri"}}
                    ]
                }]
            }
        """.trimIndent())
        }

        return response.bodyAsText()

    }

}
    data class UploadModel(val uploadUrl: String)