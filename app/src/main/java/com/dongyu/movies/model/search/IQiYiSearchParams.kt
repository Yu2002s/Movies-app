package com.dongyu.movies.model.search

data class IQiYiSearchParams(
    val name: String,
    val year: Int? = null,
    var selection: Int = 1
)