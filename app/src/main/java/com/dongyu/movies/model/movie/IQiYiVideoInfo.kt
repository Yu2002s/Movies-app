package com.dongyu.movies.model.movie

/**
 * 爱奇艺搜索视频信息（用于获取播放地址以帮助弹幕功能正常使用）
 */
data class IQiYiVideoInfo(
    val code: String,
    val data: VideoInfo
) {
    data class VideoInfo(
        /**
         * 各大平台播放地址
         */
        val playUrl: String
    )
}