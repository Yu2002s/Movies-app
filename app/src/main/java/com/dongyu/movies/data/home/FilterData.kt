package com.dongyu.movies.data.home

import com.google.gson.annotations.Expose

data class FilterData(
    val id: String,
    val name: String,
    val items: List<Item>
) {
    data class Item(
        val id: String?,
        val value: String,
        @Expose(deserialize = false)
        var isSelect: Boolean = false
    )
}
