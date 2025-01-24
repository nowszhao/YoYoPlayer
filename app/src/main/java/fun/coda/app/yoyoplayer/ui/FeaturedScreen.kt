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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import android.util.Log

@Composable
fun FeaturedScreen(
    viewModel: MainViewModel = viewModel(),
    onPlayVideo: (String) -> Unit
) {
    Log.d("FeaturedScreen", "重组 FeaturedScreen")
    val videoList by viewModel.videoList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentSource by viewModel.dataSource.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    
    // 包装 onPlayVideo 回调以添加日志
    val wrappedOnPlayVideo: (String) -> Unit = { url ->
        Log.d("FeaturedScreen", "触发播放视频: $url")
        onPlayVideo(url)
    }

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
                        onPlayVideo = wrappedOnPlayVideo,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MainViewModel.DataSource.ONLINE -> {
                    Log.d("FeaturedScreen", "显示在线视频列表，共 ${videoList.size} 个视频")
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
                                    onVideoSelected = wrappedOnPlayVideo,
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
                            onVideoSelected = wrappedOnPlayVideo,
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
    val gridState = rememberLazyGridState()
    val focusRequester = remember { FocusRequester() }
    var currentFocusIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        Log.d("VideoGrid", "请求初始焦点")
        focusRequester.requestFocus()
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        state = gridState,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { keyEvent ->
                Log.d("VideoGrid", "收到按键事件: Key code: ${keyEvent.nativeKeyEvent?.keyCode}, 原始键值: ${keyEvent.key}, 类型: ${keyEvent.type}")
                when {
                    keyEvent.type == KeyEventType.KeyDown -> {
                        when (keyEvent.nativeKeyEvent?.keyCode) {
                            android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                if (currentFocusIndex < videos.size - 1) {
                                    Log.d("VideoGrid", "向右移动焦点: $currentFocusIndex -> ${currentFocusIndex + 1}")
                                    currentFocusIndex++
                                    true
                                } else false
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                                if (currentFocusIndex > 0) {
                                    Log.d("VideoGrid", "向左移动焦点: $currentFocusIndex -> ${currentFocusIndex - 1}")
                                    currentFocusIndex--
                                    true
                                } else false
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                val columnsCount = (gridState.layoutInfo.viewportSize.width / 312).toInt()
                                if (currentFocusIndex + columnsCount < videos.size) {
                                    Log.d("VideoGrid", "向下移动焦点: $currentFocusIndex -> ${currentFocusIndex + columnsCount}")
                                    currentFocusIndex += columnsCount
                                    true
                                } else false
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                val columnsCount = (gridState.layoutInfo.viewportSize.width / 312).toInt()
                                if (currentFocusIndex - columnsCount >= 0) {
                                    Log.d("VideoGrid", "向上移动焦点: $currentFocusIndex -> ${currentFocusIndex - columnsCount}")
                                    currentFocusIndex -= columnsCount
                                    true
                                } else false
                            }
                            android.view.KeyEvent.KEYCODE_ENTER,
                            android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                            android.view.KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                if (currentFocusIndex < videos.size) {
                                    Log.d("VideoGrid", "按下确认键，当前焦点索引: $currentFocusIndex")
                                    Log.d("VideoGrid", "尝试播放视频: ${videos[currentFocusIndex].title}")
                                    Log.d("VideoGrid", "视频URL: ${videos[currentFocusIndex].videoUrl}")
                                    onVideoSelected(videos[currentFocusIndex].videoUrl)
                                    true
                                } else false
                            }
                            else -> {
                                Log.d("VideoGrid", "未处理的按键代码: ${keyEvent.nativeKeyEvent?.keyCode}")
                                false
                            }
                        }
                    }
                    else -> false
                }
            }
    ) {
        items(videos.size) { index ->
            EnhancedVideoCard(
                video = videos[index],
                isFocused = index == currentFocusIndex,
                onVideoSelected = onVideoSelected,
                modifier = Modifier.focusable()
            )
        }
    }

    // 自动滚动到当前焦点项
    LaunchedEffect(currentFocusIndex) {
        Log.d("VideoGrid", "滚动到焦点项: $currentFocusIndex")
        val columnsCount = (gridState.layoutInfo.viewportSize.width / 312).toInt()
        val targetRow = currentFocusIndex / columnsCount
        val viewportHeight = gridState.layoutInfo.viewportSize.height
        val itemHeight = 312 * 9 / 16 // 按16:9比例计算
        
        gridState.animateScrollToItem(
            index = (targetRow * columnsCount).coerceAtMost(videos.size - 1),
            scrollOffset = -(viewportHeight - itemHeight) / 2
        )
    }
}

@Composable
private fun EnhancedVideoCard(
    video: VideoListItem,
    isFocused: Boolean,
    onVideoSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 1.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = if (isFocused) {
            BorderStroke(
                width = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
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
                    video.tags?.take(2)?.forEach { tag ->
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