data class VideoListItem(
    val source: String,
    val tags: List<String>,
    val videoUrl: String,
    val title: String = "",
    val duration: Int = 0,
    val thumbnail: String = "",
    val isLoading: Boolean = false,
    val error: String = ""
) 