package `fun`.coda.app.yoyoplayer.utils

import android.util.Log
import `fun`.coda.app.yoyoplayer.network.BiliVideoService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import `fun`.coda.app.yoyoplayer.network.VideoFormat
import `fun`.coda.app.yoyoplayer.network.VideoPage
import `fun`.coda.app.yoyoplayer.network.UgcSeason

private const val TAG = "BiliVideoParser"

class BiliVideoParser {
    private val service = Retrofit.Builder()
        .baseUrl("https://api.bilibili.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BiliVideoService::class.java)

    suspend fun parseVideoUrl(url: String): VideoInfo {
        try {
            Log.d(TAG, "Parsing URL: $url")
            
            val bvid = extractBvid(url)
            val pageNum = extractPage(url)
            
            Log.d(TAG, "Extracted BVID: $bvid, Page: $pageNum")
            
            // 首先获取视频信息以获取 cid
            val infoResponse = service.getVideoInfo(bvid)
            if (!infoResponse.isSuccessful || infoResponse.body()?.code != 0) {
                throw Exception("获取视频信息失败: ${infoResponse.body()?.message}")
            }
            
            val videoData = infoResponse.body()?.data
                ?: throw Exception("未找到视频信息")
                
            // 获取对应分P的 cid
            val cid = if (pageNum > 1) {
                videoData.pages.find { it.page == pageNum }?.cid
                    ?: throw Exception("未找到第 $pageNum 分P")
            } else {
                videoData.cid
            }
            
            Log.d(TAG, "Got CID: $cid")
            
            // 获取视频 URL，尝试不同的 quality
            val qualities = listOf(80, 64, 32, 16) // 1080P, 720P, 480P, 360P
            var lastError: Exception? = null
            
            for (quality in qualities) {
                try {
                    val urlResponse = service.getVideoUrl(
                        bvid = bvid,
                        cid = cid,
                        quality = quality
                    )
                    Log.d(TAG, "URL Response for quality $quality: ${urlResponse.body()}")
                    
                    if (urlResponse.isSuccessful && urlResponse.body()?.code == 0) {
                        val data = urlResponse.body()?.data
                        if (data?.durl != null && data.durl.isNotEmpty()) {
                            return VideoInfo(
                                url = data.durl.first().url,
                                qualities = data.accept_quality,
                                formats = data.support_formats,
                                title = videoData.title,
                                currentPage = videoData.pages.find { it.cid == cid } ?: throw Exception("未找到当前分P"),
                                isPlaylist = videoData.videos > 1,
                                pages = videoData.pages,
                                ugcSeason = videoData.ugc_season
                            )
                        }
                    }
                } catch (e: Exception) {
                    lastError = e
                    Log.e(TAG, "Error getting URL for quality $quality", e)
                }
            }
            
            throw lastError ?: Exception("无法获取视频地址")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing video URL", e)
            throw e
        }
    }

    fun extractBvid(url: String): String {
        val regex = "BV[a-zA-Z0-9]+".toRegex()
        return regex.find(url)?.value ?: throw Exception("Invalid URL")
    }

    private fun extractPage(url: String): Int {
        val regex = "p=([0-9]+)".toRegex()
        return regex.find(url)?.groupValues?.get(1)?.toInt() ?: 1
    }
}

data class VideoInfo(
    val url: String,
    val qualities: List<Int>,
    val formats: List<VideoFormat>,
    val title: String,
    val currentPage: VideoPage,
    val isPlaylist: Boolean,
    val pages: List<VideoPage>,
    val ugcSeason: UgcSeason?
) 