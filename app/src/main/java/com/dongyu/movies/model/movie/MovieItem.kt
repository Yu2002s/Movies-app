package com.dongyu.movies.model.movie

import android.os.Parcel
import android.os.Parcelable

/**
 * 影视项（列表中每一个影视模型）
 */
data class MovieItem(
    /**
     * 类型
     */
    var type: String = "",
    /**
     * 地区
     */
    var area: String = "",
    /**
     * 评分
     */
    var score: Float = 0f,
    /**
     * 详细信息
     */
    var detail: String = "",
    /**
     * 年份
     */
    var years: String = "",
    /**
     * 标签列表
     */
    var tags: List<String> = emptyList(),
    /**
     * 首映日期
     */
    var premiere: String,
) : BaseMovieItem() {
    constructor() : this("", "", 0f, "", "", emptyList(), "")
}