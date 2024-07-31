package com.dongyu.movies.data.search

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dongyu.movies.data.movie.BaseMovieItem
import com.dongyu.movies.network.SearchRepository

class SearchPagingSource(private val searchParam: SearchParam) :
    PagingSource<Int, BaseMovieItem>() {
    override fun getRefreshKey(state: PagingState<Int, BaseMovieItem>) = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BaseMovieItem> {
        val page = params.key ?: 1

        val result =
            SearchRepository.getSearchTVList(searchParam.movieId, searchParam.name, page)
        val searchResult = result.getOrElse {
            return LoadResult.Error(it)
        }
        val next = if (page >= searchResult.lastPage) null else page + 1
        return LoadResult.Page(searchResult.result, null, next)
    }

}