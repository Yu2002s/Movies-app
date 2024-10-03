package com.dongyu.movies.model.search

import com.dongyu.movies.model.movie.BaseMovieItem

data class SearchResult(
    val total: Int,
    val currentPage: Int,
    val lastPage: Int,
    val result: List<BaseMovieItem>
)