package com.dongyu.movies.data.search

import com.dongyu.movies.data.movie.BaseMovieItem

data class SearchResult(
    val total: Int,
    val currentPage: Int,
    val lastPage: Int,
    val result: List<BaseMovieItem>
)