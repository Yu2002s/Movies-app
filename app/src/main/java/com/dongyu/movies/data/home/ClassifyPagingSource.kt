package com.dongyu.movies.data.home

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dongyu.movies.data.movie.BaseMovieItem
import com.dongyu.movies.network.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow

class ClassifyPagingSource(
    private val classifyQueryParam: ClassifyQueryParam,
    private val filterData: MutableStateFlow<List<FilterData>>
) : PagingSource<Int, BaseMovieItem>() {

    companion object {
        private const val TAG = "ClassifyPagingSource"
    }

    override fun getRefreshKey(state: PagingState<Int, BaseMovieItem>): Int {
        return 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BaseMovieItem> {
        val page = params.key ?: 1
        classifyQueryParam.page = page
        val result = HomeRepository.getClassify(classifyQueryParam)
        val data = result.getOrElse {
            Log.e(TAG, it.message.toString())
            return LoadResult.Error(it)
        }
        data.filterData?.let {
            if (it.isEmpty()) return@let
            it.forEach { data ->
                if (data.items.isNotEmpty()) {
                    data.items[0].isSelect = true
                }
            }
            filterData.emit(it)
        }
        classifyQueryParam.currentMovieId = data.movieId
        val categoryData = data.categoryData
        return LoadResult.Page(
            prevKey = null,
            nextKey = if (categoryData.result.isEmpty() || page >= categoryData.lastPage) null else page + 1,
            data = categoryData.result
        )
    }
}