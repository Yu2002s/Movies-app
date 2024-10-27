package com.dongyu.movies.model.movie

data class DouBanMovieItem(
    val name: String,
    val image: String,
    val director: List<Person>,
    val actor: List<Person>,
    val datePublished: String,
    val description: String
)

data class Person(
    val name: String
)