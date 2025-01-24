package `fun`.coda.app.yoyoplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.coda.app.yoyoplayer.ui.FeaturedScreen
import `fun`.coda.app.yoyoplayer.ui.theme.YoYoPlayerTheme
import `fun`.coda.app.yoyoplayer.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YoYoPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onPlayVideo = { url ->
                            startVideoPlayer(url)
                        }
                    )
                }
            }
        }
    }

    private fun startVideoPlayer(videoUrl: String) {
        val intent = Intent(this, VideoPlayerActivity::class.java).apply {
            putExtra("video_url", videoUrl)
        }
        startActivity(intent)
    }
}

@Composable
fun MainScreen(onPlayVideo: (String) -> Unit) {
    FeaturedScreen(onPlayVideo = onPlayVideo)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoInputScreen(onPlayVideo: (String) -> Unit) {
    var videoUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = videoUrl,
            onValueChange = { videoUrl = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            label = { Text("输入B站视频地址") }
        )

        Button(
            onClick = { 
                if (videoUrl.isNotBlank()) {
                    onPlayVideo(videoUrl)
                }
            },
            modifier = Modifier.width(200.dp)
        ) {
            Text("播放视频")
        }
    }
}