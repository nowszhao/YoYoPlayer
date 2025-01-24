package `fun`.coda.app.yoyoplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import VideoListItem
import `fun`.coda.app.yoyoplayer.viewmodel.MainViewModel

@Composable
fun FeaturedScreen(
    onPlayVideo: (String) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val videoList by viewModel.videoList.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(videoList) { video ->
            VideoListItemCard(
                video = video,
                onPlayVideo = onPlayVideo
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoListItemCard(
    video: VideoListItem,
    onPlayVideo: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onPlayVideo(video.videoUrl) }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "来源: ${video.source}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "标签: ${video.tags.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 