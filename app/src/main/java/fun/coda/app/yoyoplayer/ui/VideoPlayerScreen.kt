package `fun`.coda.app.yoyoplayer.ui

import androidx.compose.animation.AnimatedVisibility
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

private const val TAG = "VideoPlayerScreen"

@Composable
fun VideoPlayerScreen(
    videoInfo: VideoInfo,
    player: ExoPlayer,
    onPageSelected: (VideoPage) -> Unit,
    onQualitySelected: (Int) -> Unit,
    onSubtitleSelected: (SubtitleItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlaylist by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    var selectedSubtitle by remember { mutableStateOf<SubtitleItem?>(null) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // 视频播放区域
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = true
                    setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                        if (visibility == View.VISIBLE) {
                            showPlaylist = false
                        }
                    })
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .focusable(true)
                .onKeyEvent { keyEvent ->
                    when {
                        keyEvent.type == KeyEventType.KeyDown -> {
                            when (keyEvent.nativeKeyEvent?.keyCode) {
                                KeyEvent.KEYCODE_DPAD_CENTER,
                                KeyEvent.KEYCODE_ENTER,
                                KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                    // 切换播放/暂停状态
                                    if (player.isPlaying) {
                                        player.pause()
                                    } else {
                                        player.play()
                                    }
                                    true
                                }
                                KeyEvent.KEYCODE_DPAD_LEFT -> {
                                    // 快退10秒
                                    val newPosition = (player.currentPosition - 10_000).coerceAtLeast(0)
                                    player.seekTo(newPosition)
                                    true
                                }
                                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                    // 快进10秒
                                    val newPosition = (player.currentPosition + 10_000)
                                        .coerceAtMost(player.duration)
                                    player.seekTo(newPosition)
                                    true
                                }
                                else -> false
                            }
                        }
                        else -> false
                    }
                }
        )
        
        // 播放列表抽屉
        AnimatedVisibility(
            visible = showPlaylist,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(360.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ) {
                EnhancedPlaylistContent(
                    videoInfo = videoInfo,
                    selectedIndex = selectedIndex,
                    onPageSelected = { page, index ->
                        selectedIndex = index
                        onPageSelected(page)
                    }
                )
            }
        }
        
        // 控制按钮
        ControlButtons(
            videoInfo = videoInfo,
            showPlaylist = showPlaylist,
            onPlaylistToggle = { showPlaylist = !showPlaylist },
            onQualityClick = { showQualityDialog = true },
            onSubtitleClick = { showSubtitleDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
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
}

@Composable
private fun PlaylistContent(
    videoInfo: VideoInfo,
    currentPage: VideoPage,
    selectedIndex: Int,
    onPageSelected: (VideoPage) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 标题栏
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 3.dp
        ) {
            Text(
                text = videoInfo.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
        
        // 自动滚动到选中项
        LaunchedEffect(selectedIndex) {
            listState.animateScrollToItem(
                index = selectedIndex,
                scrollOffset = 0  // 将选中项滚动到列表顶部
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            state = listState
        ) {
            items(videoInfo.pages) { page ->
                val isSelected = page == videoInfo.pages[selectedIndex]
                VideoPageItem(
                    page = page,
                    isSelected = isSelected,
                    onClick = { onPageSelected(page) },
                    modifier = Modifier
                        .focusable(true)
                        .onKeyEvent { event ->
                            when {
                                event.type == KeyEventType.KeyDown && event.key == Key.Enter -> {
                                    onPageSelected(page)
                                    true
                                }
                                else -> false
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun VideoPageItem(
    page: VideoPage,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = RoundedCornerShape(4.dp),  // 添加圆角
        tonalElevation = if (isSelected) 8.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically  // 添加垂直居中对齐
        ) {
            // 添加序号
            Text(
                text = "${page.page}",
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.width(32.dp)
            )
            
            // 缩略图
            AsyncImage(
                model = page.pic,
                contentDescription = null,
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(16f / 9f)
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
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = formatDuration(page.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
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
private fun ControlButtons(
    videoInfo: VideoInfo,
    showPlaylist: Boolean,
    onPlaylistToggle: () -> Unit,
    onQualityClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (videoInfo.isPlaylist) {
            val interactionSource = remember { MutableInteractionSource() }
            IconButton(
                onClick = onPlaylistToggle,
                modifier = Modifier.focusable(true, interactionSource)
            ) {
                Icon(
                    imageVector = if (showPlaylist) Icons.Default.Close else Icons.Default.List,
                    contentDescription = if (showPlaylist) "关闭播放列表" else "显示播放列表"
                )
            }
        }
        
        // 添加字幕按钮
        if (videoInfo.subtitles.isNotEmpty()) {
            IconButton(
                onClick = onSubtitleClick,
                modifier = Modifier.focusable(true)
            ) {
                Icon(
                    imageVector = Icons.Default.Subtitles,
                    contentDescription = "字幕设置"
                )
            }
        }
        
        IconButton(
            onClick = onQualityClick,
            modifier = Modifier.focusable(true)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "清晰度设置"
            )
        }
    }
}

@Composable
private fun EnhancedPlaylistContent(
    videoInfo: VideoInfo,
    selectedIndex: Int,
    onPageSelected: (VideoPage, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 标题栏
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 3.dp
        ) {
            Text(
                text = videoInfo.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
        
        // 自动滚动到选中项
        LaunchedEffect(selectedIndex) {
            listState.animateScrollToItem(
                index = selectedIndex,
                scrollOffset = 0  // 将选中项滚动到列表顶部
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            state = listState
        ) {
            items(videoInfo.pages) { page ->
                val isSelected = page == videoInfo.pages[selectedIndex]
                VideoPageItem(
                    page = page,
                    isSelected = isSelected,
                    onClick = { onPageSelected(page, videoInfo.pages.indexOf(page)) },
                    modifier = Modifier
                        .focusable(true)
                        .onKeyEvent { event ->
                            when {
                                event.type == KeyEventType.KeyDown && event.key == Key.Enter -> {
                                    onPageSelected(page, videoInfo.pages.indexOf(page))
                                    true
                                }
                                else -> false
                            }
                        }
                )
            }
        }
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