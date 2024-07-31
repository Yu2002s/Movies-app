package com.dongyu.movies.data.history

import com.dongyu.movies.data.movie.Video
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

data class PlayHistory(
    val id: Long = 0,
    // 影视id
    val movieId: Int = 1,
    // 详情id
    val detailId: String = "",
    // 路线id
    var routeId: Int? = null,
    // 当前第几集
    var selection: Int? = null,
    // 共几集
    var totalSelection: Int = 0,
    // 观看当前进度
    var current: Long = 0,
    // 视频长度
    var duration: Long = 0,
    // 进度百分比
    var progress: Int = 0,
    // 名称
    var name: String = "",
    // 封面
    var cover: String? = null,
    // 创建时间
    val createAt: Long = System.currentTimeMillis(),
    // 更新时间
    var updateAt: Long = System.currentTimeMillis(),
    var video: Video = Video(),

    @Column(ignore = true)
    var timeStr: String = ""
): LitePalSupport() {

    fun update() {
        update(id)
    }
}
