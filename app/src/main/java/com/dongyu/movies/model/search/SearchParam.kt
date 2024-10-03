package com.dongyu.movies.model.search

data class SearchParam(
    var name: String = "",
    var movieId: Int? = null,
    var page: Int = 1,
    var searchUrl: String = "",
    var parseId: Int = 1,
) {
    constructor(param: SearchParam) : this(
        name = param.name,
        movieId = param.movieId,
        page = param.page,
        searchUrl = param.searchUrl,
        parseId = param.parseId
    )

    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (movieId ?: 0)
        result = 31 * result + page
        result = 31 * result + searchUrl.hashCode()
        result = 31 * result + parseId
        return result
    }
}