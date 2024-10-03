package com.dongyu.movies.model.home

import com.google.gson.annotations.Expose

data class FilterData(
    var id: String = "",
    var name: String = "",
    var items: List<Item> = emptyList()
) {

    companion object {

        /**
         * 分类（类型）
         */
        const val FILTER_CATE: String = "cateId"

        /**
         * 剧情类型
         */
        const val FILTER_TYPE: String = "type"

        /**
         * 地区
         */
        const val FILTER_AREA: String = "area"

        /**
         * 语言
         */
        const val FILTER_LANGUAGE: String = "language"

        /**
         * 年份
         */
        const val FILTER_YEAR: String = "year"

        /**
         * 首字母
         */
        const val FILTER_LETTER: String = "letter"

        /**
         * 排序
         */
        const val FILTER_SORT: String = "sort"

    }

    data class Item(
        var id: String = "",
        var name: String = "",
        var value: String = "",
        @Expose(deserialize = false)
        var isSelect: Boolean = false,
        /**
         * 整个filter项的id
         */
        @Expose(deserialize =  false)
        var groupId: String = ""
    )
}
