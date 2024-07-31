package com.dongyu.movies.data

data class PageResult<T>(
    val lastPage: Int,
    val result: List<T>
)
