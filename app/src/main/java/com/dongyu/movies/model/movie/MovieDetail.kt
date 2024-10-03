package com.dongyu.movies.model.movie

import com.dongyu.movies.model.parser.PlayParam

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
) {
    override fun equals(other: Any?): Boolean {
        // 不进行比较，永远不可能相等，解决刷新问题。
        return false
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}