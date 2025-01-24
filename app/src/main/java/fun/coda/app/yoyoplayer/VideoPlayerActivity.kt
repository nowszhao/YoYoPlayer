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

class VideoPlayerActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    private val videoParser by lazy { BiliVideoParser(this) }
    private val TAG = "VideoPlayerActivity"
    
    private var _errorMessage = mutableStateOf<String?>(null)
    private var _isLoading = mutableStateOf(true)
    private var _videoInfo = mutableStateOf<VideoInfo?>(null)

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

    private suspend fun loadSubtitle(subtitleItem: SubtitleItem?) {
        if (subtitleItem == null) {
            player?.clearMediaItems()
            return
        }

        try {
            val subtitleSource = SingleSampleMediaSource.Factory(DefaultDataSourceFactory(this))
                .createMediaSource(
                    MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitleItem.subtitle_url))
                        .setMimeType(MimeTypes.TEXT_VTT)  // B站字幕通常是WebVTT格式
                        .setLanguage(subtitleItem.lan)
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build(),
                    C.TIME_UNSET
                )

            player?.setMediaSource(subtitleSource)
            player?.prepare()
        } catch (e: Exception) {
            Log.e(TAG, "加载字幕失败", e)
            _errorMessage.value = "加载字幕失败: ${e.message}"
        }
    }

    private fun createPlayerListener() = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Player error", error)
            _errorMessage.value = "播放错误: ${error.message}"
            _isLoading.value = false
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _isLoading.value = false
                    _errorMessage.value = null
                }
                Player.STATE_ENDED -> _isLoading.value = false
                Player.STATE_BUFFERING -> _isLoading.value = true
                Player.STATE_IDLE -> _isLoading.value = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}

@Composable
fun VideoPlayerScreen(
    videoInfo: VideoInfo,
    player: ExoPlayer,
    onPageSelected: (VideoPage) -> Unit,
    onQualitySelected: (Int) -> Unit,
    onSubtitleSelected: (SubtitleItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQualityDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var selectedSubtitle by remember { mutableStateOf<SubtitleItem?>(null) }

    Column(modifier = modifier) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                }
            }
        )

        Button(
            onClick = { showQualityDialog = true },
            modifier = Modifier
                .padding(16.dp)
                .width(200.dp)
        ) {
            Text("切换清晰度")
        }

        Button(
            onClick = { showSubtitleDialog = true },
            modifier = Modifier
                .padding(16.dp)
                .width(200.dp)
        ) {
            Text("选择字幕")
        }
    }

    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("选择清晰度") },
            text = {
                Column {
                    videoInfo.qualities.forEach { quality ->
                        Button(
                            onClick = {
                                onQualitySelected(quality)
                                showQualityDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(getQualityText(quality))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showSubtitleDialog) {
        SubtitleSelectionDialog(
            subtitles = videoInfo.subtitles,
            selectedSubtitle = selectedSubtitle,
            onSubtitleSelected = { subtitle ->
                selectedSubtitle = subtitle
                onSubtitleSelected(subtitle)
                showSubtitleDialog = false
            },
            onDismiss = { showSubtitleDialog = false }
        )
    }
}

fun getQualityText(quality: Int): String = when(quality) {
    120 -> "4K"
    116 -> "1080P60"
    80 -> "1080P"
    64 -> "720P"
    32 -> "480P"
    16 -> "360P"
    else -> "${quality}P"
} 