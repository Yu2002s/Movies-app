package com.dongyu.movies.data.home

import com.dongyu.movies.data.PageResult
import com.dongyu.movies.data.movie.BaseMovieItem

data class CategoryData(
    val movieId: Int,
    val filterData: List<FilterData>?,
    val categoryData: PageResult<BaseMovieItem>
)
