package `fun`.coda.app.yoyoplayer.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

interface BiliVideoService {
    @GET("x/web-interface/view")
    suspend fun getVideoInfo(
        @Query("bvid") bvid: String,
        @Header("Cookie") cookie: String
    ): Response<VideoInfoResponse>

    @GET("x/player/playurl")
    suspend fun getVideoUrl(
        @Query("bvid") bvid: String,
        @Query("cid") cid: Long,
        @Query("qn") quality: Int = 120,
        @Query("fnval") fnval: Int = 1,
        @Query("fnver") fnver: Int = 0,
        @Query("fourk") fourk: Int = 1,
        @Header("Cookie") cookie: String,
        @Query("platform") platform: String = "html5",
        @Query("high_quality") highQuality: Int = 1
    ): Response<VideoUrlResponse>

    @GET("x/player/wbi/v2")
    suspend fun getSubtitleInfo(
        @Query("bvid") bvid: String,
        @Query("cid") cid: Long,
        @Header("Cookie") cookie: String
    ): Response<SubtitleInfoResponse>
}

data class VideoInfoResponse(
    val code: Int,
    val message: String,
    val data: VideoInfoData?
)

data class VideoInfoData(
    val bvid: String,
    val cid: Long,
    val title: String,
    val desc: String,
    val pic: String,
    val pages: List<VideoPage>,
    val videos: Int,
    val ugc_season: UgcSeason?
)

data class VideoPage(
    val cid: Long,
    val page: Int,
    val part: String,
    val duration: Int,
    val dimension: Dimension,
    val pic: String = ""
)

data class Dimension(
    val width: Int,
    val height: Int
)

data class UgcSeason(
    val id: Long,
    val title: String,
    val cover: String,
    val sections: List<Section>
)

data class Section(
    val episodes: List<Episode>
)

data class Episode(
    val bvid: String,
    val title: String,
    val arc: Arc
)

data class Arc(
    val pic: String,
    val duration: Int
)

data class VideoUrlResponse(
    val code: Int,
    val message: String,
    val data: VideoData?
)

data class VideoData(
    val quality: Int,
    val format: String,
    val timelength: Int,
    val accept_quality: List<Int>,
    val support_formats: List<VideoFormat>,
    val durl: List<VideoUrl>
)

data class VideoFormat(
    val quality: Int,
    val format: String,
    val description: String
)

data class VideoUrl(
    val url: String,
    val size: Long,
    val length: Int
)
data class SubtitleInfoResponse(
    val code: Int,
    val message: String,
    val data: SubtitleData?
)

data class SubtitleData(
    val aid: Long,
    val bvid: String,
    val cid: Long,
    val subtitle: SubtitleInfo?
)

data class SubtitleInfo(
    val allow_submit: Boolean,
    val lan: String,
    val lan_doc: String,
    val subtitles: List<SubtitleItem>
)

data class SubtitleItem(
    val id: Long,
    val lan: String,
    val lan_doc: String,
    val subtitle_url: String,
    val subtitle_url_v2: String,
    val type: Int,
    val id_str: String,
    val ai_type: Int,
    val ai_status: Int
)