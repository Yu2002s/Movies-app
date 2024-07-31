package com.dongyu.movies.viewmodel;

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.dongyu.movies.data.movie.MovieResponse
import com.dongyu.movies.data.search.SearchPagingSource
import com.dongyu.movies.data.search.SearchParam
import com.dongyu.movies.network.MovieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _searchMovieState = MutableStateFlow<List<MovieResponse.Movie>>(emptyList())

    val searchMovieState: StateFlow<List<MovieResponse.Movie>> get() = _searchMovieState

    private val searchFlow = MutableStateFlow<SearchParam?>(null)

    private val pageFlow = Pager(
        config = PagingConfig(
            36,
            prefetchDistance = 3,
            initialLoadSize = 36
        )
    ) {
        SearchPagingSource(searchFlow.value!!)
    }
        .flow

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchListFlow = searchFlow
        .filter { it != null && it.name.isNotBlank() }
        .flatMapLatest {
            // 如果数据为空则进行加载，避免频繁加载
            if (_searchMovieState.value.isEmpty()) {
                getMovieList()
            }
            pageFlow
        }
        .cachedIn(viewModelScope)

    suspend fun getMovieList() {
        _searchMovieState.value =
            MovieRepository.getMovieList().getOrDefault(emptyList())
    }

    fun search(searchParam: SearchParam) {
        viewModelScope.launch {
            searchFlow.emit(searchParam)
        }
    }

    fun refresh(movieId: Int) {
        searchFlow.value?.movieId = movieId
    }

}
