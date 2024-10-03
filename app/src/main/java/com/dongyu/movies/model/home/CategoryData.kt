package com.dongyu.movies.model.home

import com.dongyu.movies.model.page.PageResult
import com.dongyu.movies.model.movie.BaseMovieItem

data class CategoryData(
    val filterData: List<FilterData>? = null,
    val categoryData: PageResult<BaseMovieItem>,
)
