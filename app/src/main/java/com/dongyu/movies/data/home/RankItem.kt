package com.dongyu.movies.data.home

import com.dongyu.movies.data.movie.MovieItem

data class RankItem(
    val name: String,
    val first: MovieItem,
    val rankListItems: List<RankListItem>
) {
    data class RankListItem(
        val id: String,
        val routeId: Int,
        val name: String
    )
}
