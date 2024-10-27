package com.dongyu.movies.model.movie

import android.util.Log

/**
 * 影视列表响应实体类
 */
data class MovieResponse(
    val status: Int,
    val msg: String,
    /**
     * 影视列表
     */
    val data: List<Movie>
) {

    /**
     * 影视实体
     */
    data class Movie(
        /**
         * 影视唯一id
         */
        val id: Int,
        /**
         * 影视名称
         */
        val name: String,
        /**
         * 影视地址
         */
        val host: String,
        /**
         * 搜索地址
         */
        val searchUrl: String,

        /**
         * 详情地址
         */
        val detailUrl: String,

        /**
         * 分类地址
         */
        val classifyUrl: String,
        /**
         * 视频地址
         */
        val videoUrl: String,
        /**
         * 解析器id
         */
        val parseId: Int,

        /**
         * 默认线路id
         */
        val desc: String?,

        /**
         * 验证地址
         */
        val verifyUrl: String? = null,

        /**
         * 搜索时选中状态
         */
        var selected: Boolean = false
    ) {

        /**
         * 对后台返回的地址进行处理
         */
        private fun getRealUrl(url: String): String {
            return if (url.startsWith("http")) url else host + url
        }

        val fullClassifyUrl: String
            get() = getRealUrl(classifyUrl)

        val fullSearchUrl: String
            get() = getRealUrl(searchUrl)

        val fullDetailUrl: String
            get() = getRealUrl(detailUrl)

        val fullVideoUrl: String
            get() = getRealUrl(videoUrl)
    }

}