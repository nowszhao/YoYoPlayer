package `fun`.coda.app.yoyoplayer.utils

import VideoListItem
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonParser(private val context: Context) {
    fun parseVideoList(): List<VideoListItem> {
        val jsonString = context.assets.open("videos.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<VideoListItem>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }
} 