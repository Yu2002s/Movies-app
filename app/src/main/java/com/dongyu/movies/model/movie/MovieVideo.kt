package com.dongyu.movies.model.movie

/**
 * 影视视频
 */
data class MovieVideo(
    /**
     * 播放地址
     */
    var url: String,
    /**
     * 播放所需的请求头，一般无需设置，除非特殊情况需要进行设置
     */
    var headers: Map<String, String>? = null,
    /**
     * 播放地址列表
     */
    val urls: List<String>? = null
) {
    constructor(url: String): this(url, null, null)
}
