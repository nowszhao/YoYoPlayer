package `fun`.coda.app.yoyoplayer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun VideoPlayerScreen(
    videoInfo: VideoInfo,
    player: ExoPlayer,
    onPageSelected: (VideoPage) -> Unit,
    onQualitySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlaylist by remember { mutableStateOf(false) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // 主视频播放区域
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // 右侧播放列表抽屉
        AnimatedVisibility(
            visible = showPlaylist,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(320.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                PlaylistContent(
                    videoInfo = videoInfo,
                    currentPage = videoInfo.currentPage,
                    onPageSelected = { page ->
                        onPageSelected(page)
                        showPlaylist = false
                    }
                )
            }
        }
        
        // 控制按钮
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            if (videoInfo.isPlaylist) {
                IconButton(onClick = { showPlaylist = !showPlaylist }) {
                    Icon(
                        imageVector = if (showPlaylist) Icons.Default.Close else Icons.Default.List,
                        contentDescription = if (showPlaylist) "关闭播放列表" else "显示播放列表"
                    )
                }
            }
            
            IconButton(onClick = { /* 显示清晰度选择对话框 */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "清晰度设置"
                )
            }
        }
    }
}

@Composable
private fun PlaylistContent(
    videoInfo: VideoInfo,
    currentPage: VideoPage,
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
        
        // 分P列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(videoInfo.pages) { page ->
                VideoPageItem(
                    page = page,
                    isSelected = page.page == currentPage.page,
                    onClick = { onPageSelected(page) }
                )
            }
        }
    }
}

@Composable
private fun VideoPageItem(
    page: VideoPage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = RoundedCornerShape(0.dp),
        tonalElevation = if (isSelected) 8.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                // 标题
                Text(
                    text = "P${page.page}: ${page.part}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 时长
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