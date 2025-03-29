package dev.korryr.digitalid.ui.features.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class DetailedLoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Log request details
        Log.d("Network", "Sending request: ${request.url}")
        Log.d("Network", "Request method: ${request.method}")
        request.headers.forEach { (name, value) ->
            Log.d("Network", "Header: $name = $value")
        }

        // Measure request time
        val startTime = System.nanoTime()
        val response = chain.proceed(request)
        val endTime = System.nanoTime()

        // Log response details
        Log.d("Network", "Received response for ${request.url}")
        Log.d("Network", "Response code: ${response.code}")
        Log.d("Network", "Response time: ${(endTime - startTime) / 1e6} ms")

        return response
    }
}

// Update your NetworkModule to include this interceptor
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(DetailedLoggingInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
}