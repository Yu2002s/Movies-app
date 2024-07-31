package com.dongyu.movies.data.movie

data class IQiYiVideoInfo(
    val code: String,
    val data: VideoInfo
) {
    data class VideoInfo(
        val playUrl: String
    )
}