package com.dongyu.movies.model.home

import com.dongyu.movies.model.movie.MovieItem

data class RankItem(
    var name: String = "",
    var first: MovieItem = MovieItem(),
    var rankListItems: List<RankListItem> = emptyList()
) {
    data class RankListItem(
        var id: String = "",
        var routeId: Int = 0,
        var name: String = ""
    )
}
