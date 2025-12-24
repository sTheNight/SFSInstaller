package com.example.sfsinstaller.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import java.io.IOException

class Network {
    private val httpClient = OkHttpClient()
    suspend fun fetchDataAsString(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful)
                throw IOException("Unexpected code $response")
            response.body.string()
        }
    }
    suspend fun fetchDataAsSource(url: String): BufferedSource = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful)
            throw IOException("Unexpected code $response")
        response.body.source()
    }
}