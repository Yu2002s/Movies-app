package com.dongyu.movies.model.home

import com.dongyu.movies.model.movie.BaseMovieItem

/**
 * 主页所有数据
 */
data class MainData(
    val navList: List<NavItem>,
    val bannerList: List<BannerItem>,
    val tvList: List<MoviesCard<BaseMovieItem>>,
    val rankList: List<RankItem>
) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}