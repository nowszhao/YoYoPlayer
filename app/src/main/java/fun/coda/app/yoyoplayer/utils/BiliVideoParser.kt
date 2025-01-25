package `fun`.coda.app.yoyoplayer.utils

import android.content.Context
import android.util.Log
import `fun`.coda.app.yoyoplayer.network.BiliVideoService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import `fun`.coda.app.yoyoplayer.network.VideoFormat
import `fun`.coda.app.yoyoplayer.network.VideoPage
import `fun`.coda.app.yoyoplayer.network.UgcSeason
import `fun`.coda.app.yoyoplayer.network.SubtitleItem
import `fun`.coda.app.yoyoplayer.network.NetworkClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

private const val TAG = "BiliVideoParser"


// 创建 HttpLoggingInterceptor 实例
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}


val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .addInterceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .header("Accept", "application/json")
        // 添加其他必要的请求头
        val request = requestBuilder.build()
        chain.proceed(request)
    }
    .build()

//// 创建 Retrofit 实例
//val retrofit = Retrofit.Builder()
//    .baseUrl("https://api.bilibili.com/")
//    .addConverterFactory(GsonConverterFactory.create())
//    .client(okHttpClient)
//    .build()
//
//// 创建服务接口实例
//val biliVideoService = retrofit.create(BiliVideoService::class.java)


class BiliVideoParser(private val context: Context) {
    private val cookieManager = CookieManager(context)
    private val service = Retrofit.Builder()
        .baseUrl("https://api.bilibili.com/")
//        .client(NetworkClient.create())
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BiliVideoService::class.java)

    suspend fun parseVideoUrl(url: String): VideoInfo {
        try {
            Log.d(TAG, "Parsing URL: $url")
            
            val bvid = extractBvid(url)
            val pageNum = extractPage(url)
            
            Log.d(TAG, "Extracted BVID: $bvid, Page: $pageNum")
            
            val cookie = cookieManager.getCookie()
            
            // 首先获取视频信息以获取 cid
            val infoResponse = service.getVideoInfo(bvid, cookie)
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
            val qualities = if (cookieManager.hasCookie()) {
                listOf(120, 116, 80, 64, 32, 16) // 有登录时支持更高清晰度
            } else {
                listOf(80, 64, 32, 16)
            }
            var lastError: Exception? = null
            
            for (quality in qualities) {
                try {
                    val urlResponse = service.getVideoUrl(
                        bvid = bvid,
                        cid = cid,
                        quality = quality,
                        cookie = cookie
                    )
                    Log.d(TAG, "URL Response for quality $quality: ${urlResponse.body()}")
                    
                    if (urlResponse.isSuccessful && urlResponse.body()?.code == 0) {
                        val data = urlResponse.body()?.data
                        if (data?.durl != null && data.durl.isNotEmpty()) {
                            val subtitles = getSubtitles(bvid, cid, cookie)
                            return VideoInfo(
                                url = data.durl.first().url,
                                qualities = data.accept_quality,
                                formats = data.support_formats,
                                title = videoData.title,
                                currentPage = videoData.pages.find { it.cid == cid } ?: throw Exception("未找到当前分P"),
                                isPlaylist = videoData.videos > 1,
                                pages = videoData.pages,
                                ugcSeason = videoData.ugc_season,
                                subtitles = subtitles
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

    private suspend fun getSubtitles(bvid: String, cid: Long, cookie: String): List<SubtitleItem> {
        try {
            Log.d(TAG, "========== 开始获取字幕列表 ==========")
            Log.d(TAG, "参数信息:")
            Log.d(TAG, "- BVID: $bvid")
            Log.d(TAG, "- CID: $cid")
            Log.d(TAG, "- Cookie长度: ${cookie.length}")

            val response = service.getSubtitleInfo(bvid, cid, cookie)
            
            Log.d(TAG, "字幕API响应:")
            Log.d(TAG, "- 是否成功: ${response.isSuccessful}")
            Log.d(TAG, "- 响应码: ${response.code()}")
            Log.d(TAG, "- 响应头: ${response.headers()}")

            val subtitleData = response.body()?.data
            if (subtitleData != null) {
                Log.d(TAG, "字幕数据: $subtitleData")
            } else {
                Log.e(TAG, "字幕数据为空")
            }

            if (response.isSuccessful && response.body()?.code == 0) {
                val subtitles = response.body()?.data?.subtitle?.subtitles ?: emptyList()
                Log.d(TAG, "获取到 ${subtitles.size} 个字幕")
                subtitles.forEachIndexed { index, subtitle ->
                    Log.d(TAG, "字幕 #$index:")
                    Log.d(TAG, "- ID: ${subtitle.id}")
                    Log.d(TAG, "- 语言: ${subtitle.lan}")
                    Log.d(TAG, "- URL: ${subtitle.subtitle_url}")
                }
                return subtitles
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取字幕列表失败", e)
        } finally {
            Log.d(TAG, "========== 字幕列表获取结束 ==========")
        }
        return emptyList()
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
    val ugcSeason: UgcSeason?,
    val subtitles: List<SubtitleItem> = emptyList()
) 