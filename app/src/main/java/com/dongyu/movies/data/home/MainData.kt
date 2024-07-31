package com.dongyu.movies.data.home

import com.dongyu.movies.data.movie.BaseMovieItem

data class MainData(
    val movieId: Int = -1,
    val bannerList: List<BannerItem> = emptyList(),
    val tvList: List<MoviesCard<BaseMovieItem>> = emptyList(),
    val rankList: List<RankItem> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}