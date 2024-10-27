package com.dongyu.movies.model.search

import com.google.gson.annotations.SerializedName

/**
 * 豆瓣搜索结果
 */
data class DouBanSearchResult(
    val subjects: Subject,
    @SerializedName("smart_box")
    val smartBox: List<Subject.Item>?
)

data class Subject(
    val items: List<Item>
) {

    data class Item(
        val target_id: String,
        val target: Target,
        val target_type: String,
    )

    data class Target(
        val title: String,
        val cover_url: String,
        val year: String,
        val card_subtitle: String,
        val has_linewatch: Boolean,
    )
}