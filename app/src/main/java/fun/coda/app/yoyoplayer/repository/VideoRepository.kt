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
import `fun`.coda.app.yoyoplayer.data.source.LocalVideoDataSource
import `fun`.coda.app.yoyoplayer.data.source.RemoteVideoDataSource
import `fun`.coda.app.yoyoplayer.data.source.VideoDataSource

private const val TAG = "VideoRepository"

class VideoRepository(
    private val context: Context,
    private val jsonParser: JsonParser = JsonParser(context),
    private val videoParser: BiliVideoParser = BiliVideoParser(context)
) {
    private var currentDataSource: VideoDataSource = RemoteVideoDataSource()
    
    fun setDataSource(useRemote: Boolean) {
        currentDataSource = if (useRemote) {
            RemoteVideoDataSource()
        } else {
            LocalVideoDataSource(context)
        }
        Log.d(TAG, "切换到${if (useRemote) "远程" else "本地"}数据源")
    }

    fun getVideosWithDetails(): Flow<List<VideoListItem>> = flow {
        try {
            // 1. 加载基础视频列表
            Log.d(TAG, "开始加载视频列表...")
            val basicVideos = currentDataSource.getVideoList()
            Log.d(TAG, "基础视频列表加载完成: ${basicVideos.size} 个视频")
            
            // 发送初始状态
            val initialVideos = basicVideos.map { video ->
                VideoListItem(
                    source = video.source,
                    tags = video.tags,
                    videoUrl = video.videoUrl,
                    title = "加载中...",
                    duration = 0,
                    thumbnail = "",
                    isLoading = true,
                    error = ""
                )
            }
            emit(initialVideos)
            
            // 2. 为每个视频获取详细信息
            Log.d(TAG, "开始获取视频详细信息...")
            val results = mutableListOf<VideoListItem>()
            
            basicVideos.forEachIndexed { index, video ->
                try {
                    Log.d(TAG, "正在加载第 ${index + 1}/${basicVideos.size} 个视频: ${video.videoUrl}")
                    val videoInfo = videoParser.parseVideoUrl(video.videoUrl)
                    Log.d(TAG, "视频信息获取成功: ${videoInfo.title}")
                    
                    // 添加到结果列表
                    results.add(
                        VideoListItem(
                            source = video.source,
                            tags = video.tags,
                            videoUrl = video.videoUrl,
                            title = videoInfo.title ?: "",
                            duration = videoInfo.currentPage.duration,
                            thumbnail = videoInfo.currentPage.pic ?: "",
                            isLoading = false,
                            error = ""
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "加载视频失败: ${video.videoUrl}", e)
                    results.add(
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
                    )
                }
                
                // 发送进度更新
                emit(results.toList() + basicVideos.drop(results.size).map { v ->
                    VideoListItem(
                        source = v.source,
                        tags = v.tags,
                        videoUrl = v.videoUrl,
                        title = "等待加载...",
                        duration = 0,
                        thumbnail = "",
                        isLoading = true,
                        error = ""
                    )
                })
            }
            
            Log.d(TAG, "所有视频详情加载完成")
            emit(results)
        } catch (e: Exception) {
            Log.e(TAG, "加载视频列表失败", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        private const val TAG = "VideoRepository"
    }
} 