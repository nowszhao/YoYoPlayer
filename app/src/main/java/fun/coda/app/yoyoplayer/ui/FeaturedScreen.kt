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
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.AssistChip
import `fun`.coda.app.yoyoplayer.ui.components.LoadingIndicator
import `fun`.coda.app.yoyoplayer.ui.components.ErrorMessage
import `fun`.coda.app.yoyoplayer.ui.components.NavigationButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import `fun`.coda.app.yoyoplayer.ui.components.TagBar
import androidx.compose.ui.platform.LocalContext
import `fun`.coda.app.yoyoplayer.utils.CookieManager

@Composable
fun FeaturedScreen(
    viewModel: MainViewModel = viewModel(),
    onPlayVideo: (String) -> Unit
) {
    val videoList by viewModel.videoList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentSource by viewModel.dataSource.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    
    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧导航栏
        NavigationSidebar(
            currentSource = currentSource,
            onSourceChanged = { viewModel.setDataSource(it) },
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
        )
        
        // 主内容区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (currentSource) {
                MainViewModel.DataSource.SEARCH -> {
                    SearchScreen(
                        onPlayVideo = onPlayVideo,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MainViewModel.DataSource.ONLINE -> {
                    Column {
                        // 添加标签栏
                        TagBar(
                            tags = tags,
                            selectedTag = selectedTag,
                            onTagSelected = { viewModel.selectTag(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // 视频网格
                        Box(modifier = Modifier.weight(1f)) {
                            if (isLoading && videoList.isEmpty()) {
                                LoadingIndicator(
                                    progress = loadingProgress,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            
                            error?.let {
                                ErrorMessage(
                                    message = it,
                                    onRetry = { viewModel.refresh() },
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            
                            if (!isLoading || videoList.isNotEmpty()) {
                                VideoGrid(
                                    videos = videoList,
                                    onVideoSelected = onPlayVideo,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                else -> {
                    if (isLoading && videoList.isEmpty()) {
                        LoadingIndicator(
                            progress = loadingProgress,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    error?.let {
                        ErrorMessage(
                            message = it,
                            onRetry = { viewModel.refresh() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    if (!isLoading || videoList.isNotEmpty()) {
                        VideoGrid(
                            videos = videoList,
                            onVideoSelected = onPlayVideo,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationSidebar(
    currentSource: MainViewModel.DataSource,
    onSourceChanged: (MainViewModel.DataSource) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCookieDialog by remember { mutableStateOf(false) }
    val cookieManager = CookieManager(LocalContext.current)

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "YoYo儿童播放器",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        NavigationButton(
            icon = Icons.Default.Cloud,
            text = "爸爸精选",
            selected = currentSource == MainViewModel.DataSource.ONLINE,
            onClick = { onSourceChanged(MainViewModel.DataSource.ONLINE) }
        )

        NavigationButton(
            icon = Icons.Default.Search,
            text = "搜索视频",
            selected = currentSource == MainViewModel.DataSource.SEARCH,
            onClick = { onSourceChanged(MainViewModel.DataSource.SEARCH) }
        )

        NavigationButton(
            icon = Icons.Default.Storage,
            text = "本地测试",
            selected = currentSource == MainViewModel.DataSource.LOCAL,
            onClick = { onSourceChanged(MainViewModel.DataSource.LOCAL) }
        )

        Spacer(modifier = Modifier.weight(1f))
        
        NavigationButton(
            icon = Icons.Default.Refresh,
            text = "刷新",
            onClick = onRefresh
        )

        HorizontalDivider()
        
        // 添加Cookie设置按钮
        TextButton(
            onClick = { showCookieDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("设置Cookie")
        }
    }

    if (showCookieDialog) {
        CookieSettingDialog(
            cookieManager = cookieManager,
            onDismiss = { showCookieDialog = false }
        )
    }
}

@Composable
private fun VideoGrid(
    videos: List<VideoListItem>,
    onVideoSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(videos.size) { index ->
            EnhancedVideoCard(
                video = videos[index],
                onVideoSelected = onVideoSelected
            )
        }
    }
}

@Composable
private fun EnhancedVideoCard(
    video: VideoListItem,
    onVideoSelected: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .scale(if (isFocused) 1.05f else 1f)
            .focusable(true)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .clickable { onVideoSelected(video.videoUrl) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 16.dp else 4.dp
        ),
        border = if (isFocused) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = video.thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // 视频信息覆盖层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    video.tags.take(2).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (video.duration > 0) {
                        Text(
                            text = formatDuration(video.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
            
            if (video.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
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