package com.github.klee0kai.tasktree.utils

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.File

suspend inline fun <reified T> sendToUrl(
    url: String,
    value: T,
) {
    when {
        url.startsWith("file:") -> {
            val file = File(url.substringAfter("file:"))
            file.outputStream().use {
                it.write(Json.encodeToString(value).toByteArray(Charsets.UTF_8))
            }
        }

        url.startsWith("http:") || url.startsWith("https:") -> {
            val client = HttpClient(OkHttp) {
                install(ContentNegotiation){
                    json()
                }
            }

            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(value)
            }

            client.close()
        }
    }
}