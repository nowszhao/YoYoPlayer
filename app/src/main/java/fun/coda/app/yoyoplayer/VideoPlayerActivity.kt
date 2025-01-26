package `fun`.coda.app.yoyoplayer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import `fun`.coda.app.yoyoplayer.utils.BiliVideoParser
import `fun`.coda.app.yoyoplayer.ui.theme.YoYoPlayerTheme
import `fun`.coda.app.yoyoplayer.utils.VideoInfo
import kotlinx.coroutines.launch
import `fun`.coda.app.yoyoplayer.network.VideoPage
import `fun`.coda.app.yoyoplayer.network.SubtitleItem
import `fun`.coda.app.yoyoplayer.ui.VideoPlayerScreen
import `fun`.coda.app.yoyoplayer.ui.components.SubtitleSelectionDialog
import androidx.compose.ui.graphics.Brush
import androidx.core.net.toUri
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.datasource.DefaultHttpDataSource
import javax.net.ssl.X509TrustManager
import java.net.HttpURLConnection
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.HostnameVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import android.graphics.Color
import android.util.TypedValue
import androidx.core.view.marginBottom
import `fun`.coda.app.yoyoplayer.model.SubtitleSettings
import androidx.media3.ui.CaptionStyleCompat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class VideoPlayerActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    private val videoParser by lazy { BiliVideoParser(this) }
    private val TAG = "VideoPlayerActivity"
    
    private var _errorMessage = mutableStateOf<String?>(null)
    private var _isLoading = mutableStateOf(true)
    private var _videoInfo = mutableStateOf<VideoInfo?>(null)
    private var currentSubtitleUrl = ""
    private var currentSubtitleSettings = mutableStateOf(SubtitleSettings())
    
    private var _playerView: PlayerView? = null
    
    init {
        snapshotFlow { currentSubtitleSettings.value }
            .onEach { applySubtitleStyle() }
            .launchIn(lifecycleScope)
    }
    
    fun updatePlayerView(view: PlayerView) {
        _playerView = view
        applySubtitleStyle()
    }
    
    private fun applySubtitleStyle() {
        _playerView?.let { view ->

            println("#############~~~~~~~~~~~~~~~~~~~~")

            val subtitleStyle = CaptionStyleCompat(
                Color.WHITE,
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                Color.BLACK,
                null
            )
            view.subtitleView?.setStyle(subtitleStyle)
            view.subtitleView?.setPadding(0, 0, 0, currentSubtitleSettings.value.marginBottom.toInt())
        }
    }
    
    fun updateSubtitleSettings(settings: SubtitleSettings) {
        currentSubtitleSettings.value = settings
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val videoUrl = intent.getStringExtra("video_url") ?: return
        
        player = ExoPlayer.Builder(this).build()

        setContent {
            YoYoPlayerTheme {
                val errorMessage by remember { _errorMessage }
                val isLoading by remember { _isLoading }
                val videoInfo by remember { _videoInfo }

                Box(modifier = Modifier.fillMaxSize()) {
                    videoInfo?.let { info ->
                        VideoPlayerScreen(
                            videoInfo = info,
                            player = player!!,
                            subtitleSettings = currentSubtitleSettings.value,
                            onSubtitleSettingsChanged = { settings ->
                                updateSubtitleSettings(settings)
                            },
                            onPageSelected = { page ->
                                lifecycleScope.launch {
                                    val bvid = videoParser.extractBvid(videoUrl)
                                    playVideo(bvid, page)
                                }
                            },
                            onQualitySelected = { quality ->
                                lifecycleScope.launch {
                                    val bvid = videoParser.extractBvid(videoUrl)
                                    val cid = info.currentPage.cid
                                    changeVideoQuality(bvid, cid, quality)
                                }
                            },
                            onSubtitleSelected = { subtitle ->
                                lifecycleScope.launch {
                                    loadSubtitle(subtitle)
                                }
                            },
                            onSpeedSelected = { speed ->
                                changePlaybackSpeed(speed)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(50.dp)
                                .align(Alignment.Center)
                        )
                    }

                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }

        // 初始加载视频
        lifecycleScope.launch {
            try {
                _isLoading.value = true
                _videoInfo.value = videoParser.parseVideoUrl(videoUrl)
                setupPlayer(_videoInfo.value!!)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading video", e)
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun setupPlayer(videoInfo: VideoInfo) {
        player?.apply {
            setMediaItem(MediaItem.fromUri(videoInfo.url))
            
            prepare()
            playWhenReady = true
            addListener(createPlayerListener())
        }
    }

    private suspend fun playVideo(bvid: String, page: VideoPage) {
        try {
            _isLoading.value = true
            val newVideoInfo = videoParser.parseVideoUrl("https://www.bilibili.com/video/$bvid?p=${page.page}")
            _videoInfo.value = newVideoInfo
            setupPlayer(newVideoInfo)
        } catch (e: Exception) {
            _errorMessage.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    private fun changeVideoQuality(bvid: String, cid: Long, quality: Int) {
        lifecycleScope.launch {
            try {
                val videoInfo = videoParser.parseVideoUrl("https://www.bilibili.com/video/$bvid?p=$cid")
                player?.setMediaItem(MediaItem.fromUri(videoInfo.url))
                player?.prepare()
                player?.play()
            } catch (e: Exception) {
                Log.e(TAG, "Error changing quality", e)
            }
        }
    }

    private fun changePlaybackSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
    }

    private suspend fun loadSubtitle(subtitleItem: SubtitleItem?) {
        if (subtitleItem == null) {
            Log.d(TAG, "字幕项为空，清除字幕")
            player?.clearMediaItems()
            return
        }

        try {
            // 打印详细的字幕信息
            Log.d(TAG, "========== 开始加载字幕 ==========")
            Log.d(TAG, "原始字幕URL: ${subtitleItem.subtitle_url}")
            Log.d(TAG, "字幕语言: ${subtitleItem.lan}")
            Log.d(TAG, "字幕ID: ${subtitleItem.id}")
            Log.d(TAG, "字幕类型: ${subtitleItem.type}")
            Log.d(TAG, "AI类型: ${subtitleItem.ai_type}")
            Log.d(TAG, "AI状态: ${subtitleItem.ai_status}")

            // 检查URL是否包含http/https
            val subtitleUrl = if (!subtitleItem.subtitle_url.startsWith("http")) {
                "https:${subtitleItem.subtitle_url}"
            } else {
                subtitleItem.subtitle_url
            }
            Log.d(TAG, "处理后的字幕URL: $subtitleUrl")

            // 在IO线程中执行网络请求
            withContext(Dispatchers.IO) {
                try {
                    // 创建信任所有证书的TrustManager
                    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    })

                    // 创建SSLContext并初始化
                    val sslContext = SSLContext.getInstance("SSL")
                    sslContext.init(null, trustAllCerts, java.security.SecureRandom())

                    // 创建OkHttpClient
                    val client = OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                        .hostnameVerifier { _, _ -> true }
                        .addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                        .build()

                    val request = Request.Builder()
                        .url(subtitleUrl)
                        .addHeader("Accept", "*/*")
                        .addHeader("Origin", "https://www.bilibili.com")
                        .addHeader("Referer", "https://www.bilibili.com")
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .build()

                    Log.d(TAG, "正在使用OkHttp请求字幕内容...")
                    val response = client.newCall(request).execute()
                    Log.d(TAG, "字幕请求响应码: ${response.code}")
                    Log.d(TAG, "字幕请求响应头: ${response.headers}")
                    
                    if (response.isSuccessful) {
                        val subtitleContent = response.body?.string()
                        Log.d(TAG, "成功获取字幕内容，前100字符: ${subtitleContent?.take(100)}")
                        
                        // 解析JSON字幕并转换为VTT格式
                        val vttContent = convertJsonToVtt(subtitleContent)
                        
                        // 将VTT内容写入临时文件
                        val subtitleFile = File(applicationContext.cacheDir, "subtitle_${subtitleItem.id}.vtt")
                        subtitleFile.writeText(vttContent)
                        
                        // 更新字幕URL
                        currentSubtitleUrl = subtitleFile.toUri().toString()
                        Log.d(TAG, "转换后的字幕文件路径: $currentSubtitleUrl")
                    } else {
                        Log.e(TAG, "字幕请求失败: ${response.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "OkHttp请求字幕失败", e)
                }
            }

            val currentPosition = player?.currentPosition ?: 0
            val wasPlaying = player?.isPlaying ?: false
            val currentMediaItem = player?.currentMediaItem

            if (currentMediaItem != null) {
                // 创建字幕配置，使用更新后的currentSubtitleUrl
                val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(Uri.parse(currentSubtitleUrl))
                    .setMimeType(MimeTypes.TEXT_VTT)
                    .setLanguage(subtitleItem.lan)
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()

                Log.d(TAG, "字幕配置信息:")
                Log.d(TAG, "- URI: ${subtitleConfig.uri}")
                Log.d(TAG, "- MimeType: ${subtitleConfig.mimeType}")
                Log.d(TAG, "- Language: ${subtitleConfig.language}")
                Log.d(TAG, "- Selection Flags: ${subtitleConfig.selectionFlags}")

                // 创建新的MediaItem
                val newMediaItem = currentMediaItem.buildUpon()
                    .setSubtitleConfigurations(listOf(subtitleConfig))
                    .build()

                Log.d(TAG, "设置新的MediaItem，包含字幕配置")
                Log.d(TAG, "MediaItem信息:")
                Log.d(TAG, "- MediaId: ${newMediaItem.mediaId}")
                Log.d(TAG, "- LocalConfiguration: ${newMediaItem.localConfiguration}")

                player?.setMediaItem(newMediaItem)
                player?.prepare()
                player?.seekTo(currentPosition)
                if (wasPlaying) {
                    player?.play()
                }
            } else {
                Log.e(TAG, "当前MediaItem为空")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载字幕失败", e)
            _errorMessage.value = "加载字幕失败: ${e.message}"
        } finally {
            Log.d(TAG, "========== 字幕加载结束 ==========")
        }
    }

    private fun convertJsonToVtt(jsonContent: String?): String {
        if (jsonContent == null) return ""
        
        val sb = StringBuilder()
        sb.appendLine("WEBVTT")
        sb.appendLine()
        
        try {
            val jsonObject = JSONObject(jsonContent)
            val body = jsonObject.getJSONArray("body")
            
            for (i in 0 until body.length()) {
                val item = body.getJSONObject(i)
                val from = item.getDouble("from")
                val to = item.getDouble("to")
                val content = item.getString("content")
                
                // 转换时间格式 (秒转换为 HH:MM:SS.mmm)
                val fromTime = formatTime(from)
                val toTime = formatTime(to)
                
                sb.appendLine("${fromTime} --> ${toTime}")
                sb.appendLine(content)
                sb.appendLine()
            }
        } catch (e: Exception) {
            Log.e(TAG, "转换字幕格式失败", e)
        }
        
        return sb.toString()
    }

    private fun formatTime(seconds: Double): String {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        val secs = seconds % 60
        return String.format("%02d:%02d:%06.3f", hours, minutes, secs)
    }

    private fun createPlayerListener(): Player.Listener {
        return object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_READY -> {
                        _isLoading.value = false
                    }
                    Player.STATE_BUFFERING -> {
                        _isLoading.value = true
                    }
                    Player.STATE_ENDED -> {
                        player?.seekTo(0)
                        player?.pause()
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                _errorMessage.value = "播放错误: ${error.message}"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
} 