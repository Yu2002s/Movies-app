package com.dongyu.movies.data.page

data class PageResult<T>(
    val lastPage: Int,
    val result: List<T>
)
