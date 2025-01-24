package `fun`.coda.app.yoyoplayer.utils

import VideoListItem
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log

class JsonParser(private val context: Context) {
    private val gson = Gson()
    private val videoListType = object : TypeToken<List<VideoListItem>>() {}.type

    fun parseVideoList(): List<VideoListItem> {
        Log.d("", "开始解析视频列表文件")
        val jsonString = context.assets.open("videos.json").bufferedReader().use { it.readText() }
        Log.d("", "读取到的 JSON 内容: $jsonString")
        val videos = gson.fromJson<List<VideoListItem>>(jsonString, videoListType)
        Log.d("", "解析完成，共 ${videos.size} 个视频")
        return videos
    }
} 