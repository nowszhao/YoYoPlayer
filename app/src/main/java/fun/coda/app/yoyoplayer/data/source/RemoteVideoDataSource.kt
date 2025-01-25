package `fun`.coda.app.yoyoplayer.data.source

import VideoListItem
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import `fun`.coda.app.yoyoplayer.network.NetworkClient
import `fun`.coda.app.yoyoplayer.utils.SettingsManager

class RemoteVideoDataSource(
    private val settingsManager: SettingsManager,
    private val client: OkHttpClient = NetworkClient.create(),
    private val gson: Gson = Gson()
) : VideoDataSource {
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.bilibili.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    override suspend fun getVideoList(): List<VideoListItem> = withContext(Dispatchers.IO) {
        val baseUrl = settingsManager.baseUrl
        Log.d(TAG, "从远程加载视频列表: $baseUrl")
        
        val request = Request.Builder()
            .url(baseUrl)
            .build()
            
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("请求失败: ${response.code}")
            }
            
            val jsonString = response.body?.string()
                ?: throw IOException("响应体为空")
                
            Log.d(TAG, "获取到的JSON数据: $jsonString")
            
            val videoListType = object : TypeToken<List<VideoListItem>>() {}.type
            gson.fromJson<List<VideoListItem>>(jsonString, videoListType)
                .also { Log.d(TAG, "解析完成，共 ${it.size} 个视频") }
        } catch (e: Exception) {
            Log.e(TAG, "加载远程视频列表失败", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "RemoteVideoDataSource"
    }
} 