package `fun`.coda.app.yoyoplayer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import `fun`.coda.app.yoyoplayer.network.VideoPage
import `fun`.coda.app.yoyoplayer.utils.VideoInfo
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.view.View
import androidx.compose.material.icons.filled.Subtitles
import `fun`.coda.app.yoyoplayer.network.SubtitleItem
import `fun`.coda.app.yoyoplayer.ui.components.SubtitleSelectionDialog
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import `fun`.coda.app.yoyoplayer.ui.components.VideoControlBar
import `fun`.coda.app.yoyoplayer.ui.components.SpeedSelectionDialog
import androidx.compose.ui.graphics.Color
import android.util.Log

private const val TAG = "VideoPlayerScreen"

@Composable
fun VideoPlayerScreen(
    videoInfo: VideoInfo,
    player: ExoPlayer,
    onPageSelected: (VideoPage) -> Unit,
    onQualitySelected: (Int) -> Unit,
    onSubtitleSelected: (SubtitleItem?) -> Unit,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlaylist by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var selectedSubtitle by remember { mutableStateOf<SubtitleItem?>(null) }
    var showControls by remember { mutableStateOf(false) }
    var isVideoAreaFocused by remember { mutableStateOf(true) }
    
    // 确保 FocusRequester 在整个组件生命周期中保持稳定
    val videoAreaFocusRequester = remember { FocusRequester() }
    val controlBarFocusRequester = remember { FocusRequester() }

    // 初始化焦点
    LaunchedEffect(Unit) {
        videoAreaFocusRequester.requestFocus()
    }

    // 处理返回键
    BackHandler(enabled = showPlaylist || showControls) {
        if (showPlaylist) {
            showPlaylist = false
        } else if (showControls) {
            showControls = false
            player.play()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { keyEvent ->
                    when {
                        keyEvent.type == KeyEventType.KeyDown -> {
                            when (keyEvent.nativeKeyEvent?.keyCode) {
                                KeyEvent.KEYCODE_DPAD_CENTER, 
                                KeyEvent.KEYCODE_ENTER -> {
                                    if (!showControls) {
                                        showControls = true
                                        player.pause()
                                        true
                                    } else {
                                        if (isVideoAreaFocused) {
                                            player.playWhenReady = !player.playWhenReady
                                            if (player.playWhenReady) {
                                                showControls = false
                                            }
                                            true
                                        } else false
                                    }
                                }
                                KeyEvent.KEYCODE_DPAD_DOWN -> {
                                    if (isVideoAreaFocused && showControls) {
                                        isVideoAreaFocused = false
                                        try {
                                            controlBarFocusRequester.requestFocus()
                                        } catch (e: IllegalStateException) {
                                            Log.e(TAG, "Failed to request focus for control bar", e)
                                        }
                                        true
                                    } else false
                                }
                                KeyEvent.KEYCODE_DPAD_LEFT -> {
                                    if (isVideoAreaFocused) {
                                        player.seekBack()
                                        true
                                    } else false
                                }
                                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                    if (isVideoAreaFocused) {
                                        player.seekForward()
                                        true
                                    } else false
                                }
                                else -> false
                            }
                        }
                        else -> false
                    }
                }
                .focusRequester(videoAreaFocusRequester)
                .focusable(true),
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                    
                    // 移除所有默认控件
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setShowFastForwardButton(false)
                    setShowRewindButton(false)
                    setShowShuffleButton(false)
                    setShowSubtitleButton(false)
                    setShowVrButton(false)
                }
            },
            update = { view ->
                view.player = player
            }
        )

        // 自定义控制栏
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                // 进度条
                if (player.duration > 0) {
                    Slider(
                        value = player.currentPosition.toFloat(),
                        onValueChange = { player.seekTo(it.toLong()) },
                        valueRange = 0f..player.duration.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    
                    // 时间显示
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration((player.currentPosition / 1000).toInt()),
                            color = Color.White
                        )
                        Text(
                            text = formatDuration((player.duration / 1000).toInt()),
                            color = Color.White
                        )
                    }
                }

                // 控制按钮栏
                VideoControlBar(
                    isPlaylist = videoInfo.isPlaylist,
                    hasSubtitles = videoInfo.subtitles.isNotEmpty(),
                    onPlaylistClick = { showPlaylist = !showPlaylist },
                    onSubtitleClick = { showSubtitleDialog = true },
                    onQualityClick = { showQualityDialog = true },
                    onSpeedClick = { showSpeedDialog = true },
                    onFocusedChanged = { focused ->
                        if (!focused) {
                            isVideoAreaFocused = true
                            try {
                                videoAreaFocusRequester.requestFocus()
                            } catch (e: IllegalStateException) {
                                Log.e(TAG, "Failed to request focus for video area", e)
                            }
                        }
                    },
                    modifier = Modifier.focusRequester(controlBarFocusRequester)
                )
            }
        }
        
        // 播放列表抽屉
        AnimatedVisibility(
            visible = showPlaylist,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            PlaylistDrawer(
                videoInfo = videoInfo,
                onPageSelected = { page ->
                    onPageSelected(page)
                    showPlaylist = false
                    showControls = false
                    player.play()
                },
                onDismiss = { 
                    showPlaylist = false 
                }
            )
        }
    }
    
    // 清晰度选择对话框
    if (showQualityDialog) {
        QualitySelectionDialog(
            qualities = videoInfo.qualities,
            onQualitySelected = {
                onQualitySelected(it)
                showQualityDialog = false
            },
            onDismiss = { showQualityDialog = false }
        )
    }

    // 添加字幕选择对话框
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

    if (showSpeedDialog) {
        SpeedSelectionDialog(
            currentSpeed = player.playbackParameters.speed,
            onSpeedSelected = { speed ->
                onSpeedSelected(speed)
                showSpeedDialog = false
            },
            onDismiss = { showSpeedDialog = false }
        )
    }

    // 处理控制栏显示/隐藏时的焦点
    LaunchedEffect(showControls) {
        if (showControls) {
            isVideoAreaFocused = false
            try {
                controlBarFocusRequester.requestFocus()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to request focus during controls visibility change", e)
            }
        } else {
            isVideoAreaFocused = true
            try {
                videoAreaFocusRequester.requestFocus()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to request focus during controls visibility change", e)
            }
        }
    }
}

@Composable
private fun PlaylistDrawer(
    videoInfo: VideoInfo,
    onPageSelected: (VideoPage) -> Unit,
    onDismiss: () -> Unit
) {
    var currentFocusIndex by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    // 自动获取焦点
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // 自动滚动到当前选中项
    LaunchedEffect(currentFocusIndex) {
        listState.animateScrollToItem(currentFocusIndex)
    }

    Surface(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "选集列表",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            // 播放列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onKeyEvent { keyEvent ->
                        when {
                            keyEvent.type == KeyEventType.KeyDown -> {
                                when (keyEvent.nativeKeyEvent?.keyCode) {
                                    KeyEvent.KEYCODE_DPAD_UP -> {
                                        if (currentFocusIndex > 0) {
                                            currentFocusIndex--
                                            true
                                        } else false
                                    }
                                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                                        if (currentFocusIndex < videoInfo.pages.size - 1) {
                                            currentFocusIndex++
                                            true
                                        } else false
                                    }
                                    KeyEvent.KEYCODE_DPAD_CENTER,
                                    KeyEvent.KEYCODE_ENTER -> {
                                        onPageSelected(videoInfo.pages[currentFocusIndex])
                                        true
                                    }
                                    KeyEvent.KEYCODE_BACK -> {
                                        onDismiss()
                                        true
                                    }
                                    else -> false
                                }
                            }
                            else -> false
                        }
                    }
                    .focusable(true)
            ) {
                items(videoInfo.pages) { page ->
                    PlaylistItem(
                        page = page,
                        isSelected = page == videoInfo.currentPage,
                        isFocused = videoInfo.pages.indexOf(page) == currentFocusIndex,
                        onClick = { onPageSelected(page) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    page: VideoPage,
    isSelected: Boolean,
    isFocused: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isFocused -> MaterialTheme.colorScheme.secondaryContainer
                    else -> Color.Transparent
                }
            )
            .padding(8.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 缩略图
        AsyncImage(
            model = page.pic,
            contentDescription = null,
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(16f/9f)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        
        // 文字信息
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = page.part,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isFocused || isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            Text(
                text = formatDuration(page.duration),
                style = MaterialTheme.typography.bodySmall,
                color = if (isFocused || isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                }
            )
        }
    }
}

// 格式化时长
private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, remainingSeconds)
    } else {
        String.format("%02d:%02d", minutes, remainingSeconds)
    }
}

@Composable
private fun QualitySelectionDialog(
    qualities: List<Int>,
    onQualitySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Implementation of the QualitySelectionDialog
} 