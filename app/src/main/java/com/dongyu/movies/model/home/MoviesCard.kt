package com.dongyu.movies.model.home

data class MoviesCard<T>(
    val title: String = "",
    val list: List<T>,
)