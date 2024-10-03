package com.dongyu.movies.model.home

import com.dongyu.movies.model.movie.BaseMovieItem

/**
 * 主页数据model，用于映射BRV
 */
sealed class HomeModel {

    /**
     * 主页顶部Banner
     */
    data class Banner(val bannerList: List<BannerItem>) : HomeModel()

    /**
     * 影视宫格列表
     */
    data class MoviesGrid(val cardItem: MoviesCard<BaseMovieItem>) : HomeModel()
}