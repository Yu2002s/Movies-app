package com.dongyu.movies.model.movie

data class MovieDetail(
    /**
     * 影视基本信息
     */
    val movieItem: MovieItem,
    /**
     * 当前视频项
     */
    var currentSourceItem: VideoSource.Item,
    /**
     * 播放源
     */
    val videoSources: List<VideoSource>,
    /**
     * 影视播放信息（一般为空，当详情页和播放页是同一个页面则需要进行设置）
     */
    val video: MovieVideo? = null
) {

    constructor(
        movieItem: MovieItem,
        currentSourceItem: VideoSource.Item,
        videoSources: List<VideoSource>
    ) : this(movieItem, currentSourceItem, videoSources, null)

    override fun equals(other: Any?): Boolean {
        // 不进行比较，永远不可能相等，解决刷新问题。
        return false
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}