package com.dongyu.movies.data.movie

data class MovieDetail(
    val main: MovieItem,
    val sources: List<PlaySource>,
    var url: Video?
)
