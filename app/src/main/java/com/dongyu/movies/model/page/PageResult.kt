package com.dongyu.movies.model.page

data class PageResult<T>(
    var currentPage: Int = 0,
    var lastPage: Int = 0,
    var result: List<T> = emptyList(),
    var total: Int = 0
)