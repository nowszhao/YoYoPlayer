package `fun`.coda.app.yoyoplayer.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkClient {
    fun create(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
} 