package `fun`.coda.app.yoyoplayer.viewmodel

import VideoListItem
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `fun`.coda.app.yoyoplayer.utils.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val jsonParser = JsonParser(application)
    
    private val _videoList = MutableStateFlow<List<VideoListItem>>(emptyList())
    val videoList: StateFlow<List<VideoListItem>> = _videoList
    
    init {
        loadVideos()
    }
    
    private fun loadVideos() {
        viewModelScope.launch {
            try {
                _videoList.value = jsonParser.parseVideoList()
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
} 