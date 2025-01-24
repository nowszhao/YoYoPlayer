package `fun`.coda.app.yoyoplayer.viewmodel

import VideoListItem
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `fun`.coda.app.yoyoplayer.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VideoRepository(application)
    private val TAG = "MainViewModel"
    
    private val _videoList = MutableStateFlow<List<VideoListItem>>(emptyList())
    val videoList: StateFlow<List<VideoListItem>> = _videoList
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    init {
        loadVideos()
    }
    
    private fun loadVideos() {
        viewModelScope.launch {
            Log.d(TAG, "开始加载视频...")
            _isLoading.value = true
            _loadingProgress.value = 0f
            _error.value = null
            
            try {
                repository.getVideosWithDetails().collect { videos ->
                    Log.d(TAG, "收到视频列表更新: ${videos.size} 个视频")
                    Log.d(TAG, "视频列表内容: ${videos.map { it.title }}")
                    _videoList.value = videos
                    
                    // 计算加载进度
                    val loadedCount = videos.count { !it.isLoading }
                    val totalCount = videos.size
                    if (totalCount > 0) {
                        _loadingProgress.value = loadedCount.toFloat() / totalCount
                        Log.d(TAG, "加载进度: ${(_loadingProgress.value * 100).toInt()}%")
                    }
                    
                    // 检查是否全部加载完成
                    if (loadedCount == totalCount) {
                        _isLoading.value = false
                        Log.d(TAG, "视频加载完成，共 $totalCount 个视频")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载视频失败", e)
                _error.value = e.message ?: "未知错误"
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        Log.d(TAG, "刷新视频列表")
        loadVideos()
    }
} 