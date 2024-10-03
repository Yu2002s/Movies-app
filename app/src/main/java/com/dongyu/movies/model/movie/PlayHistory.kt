package com.dongyu.movies.model.movie

import com.dongyu.movies.utils.getRelTime
import com.dongyu.movies.utils.getTime
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import java.util.Date

data class PlayHistory(
    /**
     * 影视id
     */
    val movieId: Int,
    /**
     * 详情id
     */
    val detailId: String,
    /**
     * 线路源id
     */
    var sourceId: String = "",
    /**
     * 集数id
     */
    var selectionId: String = "",
    /**
     * 封面图
     */
    var cover: String = "",
    /**
     * 名称
     */
    var name: String = "",
    /**
     * 暂时用来标记第几集(默认第一集，从0开始)
     */
    @Column(defaultValue = "0")
    var selection: Int = 0,
    /**
     * 总共多少集（默认共一集）
     */
    var totalSelection: Int = 1,
    /**
     * 当前播放时间
     */
    var currentTime: Long = 0L,
    /**
     * 视频长度
     */
    var duration: Long = 0L,
    /**
     * 视频进度
     */
    var progress: Int = 0,
    /**
     * 视频地址
     */
    val videoUrl: String = "",
    /**
     * 最后播放时间
     */
    var lastPlayTime: Long = Date().time,
    /**
     * 是否收藏
     */
    @Column(defaultValue = "0")
    var isCollected: Boolean = false,
    @Column(ignore = true)
    var lastPlayTimeStr: String = lastPlayTime.getRelTime()
) : LitePalSupport() {
    /**
     * 主键
     */
    val id: Int = 0

    val hasNextSelection get() = selection < totalSelection

    /**
     * 选中状态
     */
    @Column(ignore = true)
    var isChecked = false

    /**
     * 是否显示复选框
     */
    @Column(ignore = true)
    var isCheckable = false
}
