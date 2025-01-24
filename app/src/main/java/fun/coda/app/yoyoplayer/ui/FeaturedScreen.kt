package `fun`.coda.app.yoyoplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import VideoListItem
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import `fun`.coda.app.yoyoplayer.viewmodel.MainViewModel
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage

@Composable
fun FeaturedScreen(
    viewModel: MainViewModel = viewModel(),
    onPlayVideo: (String) -> Unit
) {
    val videoList by viewModel.videoList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val error by viewModel.error.collectAsState()
    val isRemoteSource by viewModel.dataSource.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 数据源切换栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("数据源:")
                Switch(
                    checked = isRemoteSource,
                    onCheckedChange = { viewModel.setDataSource(it) },
                    thumbContent = if (isRemoteSource) {
                        { Icon(Icons.Default.Cloud, null) }
                    } else {
                        { Icon(Icons.Default.Storage, null) }
                    }
                )
                Text(if (isRemoteSource) "远程" else "本地")
            }
            
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Default.Refresh, "刷新")
            }
        }

        // 原有的视频列表显示逻辑保持不变
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading && videoList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = loadingProgress,
                        modifier = Modifier.width(200.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "加载中... ${(loadingProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            error?.let {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.refresh() }) {
                        Text("重试")
                    }
                }
            }
            
            if (!isLoading || videoList.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(videoList.size) { index ->
                        VideoCard(
                            video = videoList[index],
                            onPlayVideo = onPlayVideo
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoCard(
    video: VideoListItem,
    onPlayVideo: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clickable(enabled = !video.isLoading) { onPlayVideo(video.videoUrl) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (video.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center)
                )
            } else {
                // 缩略图
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // 视频信息覆盖层
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 标题
                    Text(
                        text = video.title.ifEmpty { "加载中..." },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 标签
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            video.tags.forEach { tag ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(tag) }
                                )
                            }
                        }
                        
                        // 时长
                        if (video.duration > 0) {
                            Text(
                                text = formatDuration(video.duration),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

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