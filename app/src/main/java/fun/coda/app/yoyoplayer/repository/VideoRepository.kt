package `fun`.coda.app.yoyoplayer.repository

import VideoListItem
import android.util.Log
import `fun`.coda.app.yoyoplayer.utils.BiliVideoParser
import `fun`.coda.app.yoyoplayer.utils.JsonParser
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val TAG = "VideoRepository"

class VideoRepository(
    private val context: Context,
    private val jsonParser: JsonParser = JsonParser(context),
    private val videoParser: BiliVideoParser = BiliVideoParser()
) {
    fun getVideosWithDetails(): Flow<List<VideoListItem>> = flow {
        try {
            // 1. 加载基础视频列表
            Log.d(TAG, "开始加载视频列表...")
            val basicVideos = jsonParser.parseVideoList()
            Log.d(TAG, "基础视频列表加载完成: ${basicVideos.size} 个视频")
            
            // 使用安全的默认值，确保所有字段都有值
            val initialVideos = basicVideos.map { video ->
                VideoListItem(
                    source = video.source,
                    tags = video.tags,
                    videoUrl = video.videoUrl,
                    title = "",  // 使用空字符串而不是 null
                    duration = 0,
                    thumbnail = "",
                    isLoading = true,
                    error = ""
                )
            }
            emit(initialVideos)

            // 2. 为每个视频获取详细信息
            Log.d(TAG, "开始获取视频详细信息...")
            val videosWithDetails = basicVideos.mapIndexed { index, video ->
                try {
                    Log.d(TAG, "正在加载第 ${index + 1}/${basicVideos.size} 个视频: ${video.videoUrl}")
                    val videoInfo = videoParser.parseVideoUrl(video.videoUrl)
                    Log.d(TAG, "视频信息获取成功: ${videoInfo.title}")
                    
                    // 创建新的实例而不是使用 copy
                    VideoListItem(
                        source = video.source,
                        tags = video.tags,
                        videoUrl = video.videoUrl,
                        title = videoInfo.title ?: "",  // 使用 Elvis 运算符处理 null
                        duration = videoInfo.currentPage.duration,
                        thumbnail = videoInfo.currentPage.pic ?: "",
                        isLoading = false,
                        error = ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "加载视频失败: ${video.videoUrl}", e)
                    // 创建新的实例而不是使用 copy
                    VideoListItem(
                        source = video.source,
                        tags = video.tags,
                        videoUrl = video.videoUrl,
                        title = "加载失败",
                        duration = 0,
                        thumbnail = "",
                        isLoading = false,
                        error = e.message ?: "未知错误"
                    )
                }
            }
            Log.d(TAG, "所有视频详情加载完成")
            emit(videosWithDetails)
        } catch (e: Exception) {
            Log.e(TAG, "加载视频列表失败", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
} 