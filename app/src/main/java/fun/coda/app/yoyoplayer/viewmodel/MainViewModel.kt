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
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asStateFlow
import `fun`.coda.app.yoyoplayer.model.Tag

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
    
    private val _dataSource = MutableStateFlow(DataSource.ONLINE)
    val dataSource: StateFlow<DataSource> = _dataSource.asStateFlow()
    
    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags = _tags.asStateFlow()
    
    private val _selectedTag = MutableStateFlow<Tag?>(null)
    val selectedTag = _selectedTag.asStateFlow()
    
    // 添加一个新的状态来保存原始视频列表
    private val _originalVideoList = MutableStateFlow<List<VideoListItem>>(emptyList())
    
    init {
        loadVideos()
    }
    
    enum class DataSource {
        ONLINE,     // 在线视频
        SEARCH,      // 搜索
        LOCAL      // 本地视频
    }
    
    fun setDataSource(source: DataSource) {
        if (_dataSource.value != source) {
            _dataSource.value = source
            when (source) {
                DataSource.LOCAL -> repository.setDataSource(false)
                DataSource.ONLINE -> repository.setDataSource(true)
                DataSource.SEARCH -> {} // 切换到搜索模式
            }
            refresh()
        }
    }
    
    fun selectTag(tag: Tag?) {
        _selectedTag.value = tag
        filterVideosByTag()
    }
    
    private fun filterVideosByTag() {
        viewModelScope.launch {
            val tag = _selectedTag.value
            if (tag == null) {
                // 如果没有选中标签，显示所有视频
                _videoList.value = _originalVideoList.value
            } else {
                // 根据标签过滤视频
                val filteredVideos = _originalVideoList.value.filter { video ->
                    video.tags?.contains(tag.name) == true
                }
                _videoList.value = filteredVideos
            }
        }
    }
    
    private fun updateTags(videos: List<VideoListItem>) {
        // 从视频列表中提取标签
        val tagMap = mutableMapOf<String, Int>()
        videos.forEach { video ->
            video.tags?.forEach { tag ->
                tagMap[tag] = (tagMap[tag] ?: 0) + 1
            }
        }
        
        _tags.value = tagMap.map { Tag(it.key, it.value) }.sortedBy { it.name }
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
                    _originalVideoList.value = videos  // 保存原始列表
                    _videoList.value = videos
                    updateTags(videos) // 更新标签列表
                    
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