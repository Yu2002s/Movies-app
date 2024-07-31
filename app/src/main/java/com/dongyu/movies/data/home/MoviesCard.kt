package com.dongyu.movies.data.home

data class MoviesCard<T>(
    val title: String,
    val list: List<T>
)