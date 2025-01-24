package `fun`.coda.app.yoyoplayer.data.source

import VideoListItem

interface VideoDataSource {
    suspend fun getVideoList(): List<VideoListItem>
} 