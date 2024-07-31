package com.dongyu.movies.data.movie

data class MovieResponse(
    val status: Int,
    val msg: String,
    val data: List<Movie>
) {

    data class Movie(
        val id: Int,
        val name: String,
        var selected: Boolean = false
    )

}