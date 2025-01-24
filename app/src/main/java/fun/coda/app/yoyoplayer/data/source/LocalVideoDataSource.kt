package `fun`.coda.app.yoyoplayer.data.source

import VideoListItem
import android.content.Context
import `fun`.coda.app.yoyoplayer.utils.JsonParser
import android.util.Log

class LocalVideoDataSource(
    private val context: Context,
    private val jsonParser: JsonParser = JsonParser(context)
) : VideoDataSource {
    override suspend fun getVideoList(): List<VideoListItem> {
        Log.d(TAG, "从本地加载视频列表")
        return jsonParser.parseVideoList()
    }

    companion object {
        private const val TAG = "LocalVideoDataSource"
    }
} 