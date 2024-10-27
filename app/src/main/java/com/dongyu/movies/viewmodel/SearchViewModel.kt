package com.dongyu.movies.viewmodel;

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.model.movie.MovieResponse
import com.dongyu.movies.model.search.History
import com.dongyu.movies.model.search.SearchParam
import com.dongyu.movies.model.search.SearchSuggestItem
import com.dongyu.movies.model.search.SearchUiResult
import com.dongyu.movies.model.search.SearchUiState
import com.dongyu.movies.model.search.SearchUiSuggest
import com.dongyu.movies.network.MovieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    // 搜索UI状态，默认未搜索历史界面
    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiSuggest())

    val searchUiState: StateFlow<SearchUiState> = _searchUiState

    private val _searchSuggestState = MutableStateFlow<List<SearchSuggestItem>>(emptyList())
    val searchSuggestState: StateFlow<List<SearchSuggestItem>> get() = _searchSuggestState

    private val shareFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _searchMovieState = MutableStateFlow<List<MovieResponse.Movie>>(emptyList())

    val searchMovieState: StateFlow<List<MovieResponse.Movie>> get() = _searchMovieState

    val searchFlow = MutableStateFlow(SearchParam())

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchListFlow = searchFlow
        .filter { it.name.isNotBlank() }
        .flatMapLatest {
            Log.d("jdy", "search: $it")
            // 如果数据为空则进行加载，避免频繁加载
            if (_searchMovieState.value.isEmpty()) {
                getMovieList()
            }
            if (it.movieId == null) {
                val movie = _searchMovieState.value.getOrNull(0)
                it.movieId = movie?.id
            }
            if (it.searchUrl.isEmpty()) {
                emptyFlow()
            } else {
                MovieRepository.getSearchTVList(it)
            }
        }

    init {
        viewModelScope.launch {
            shareFlow.collectLatest { name ->
                // 此处发起网络请求
                _searchSuggestState.value =
                    MovieRepository.getSearchSuggest(name).single().getOrDefault(emptyList())
            }
        }

        viewModelScope.launch {
            searchUiState.collect {
                if (it is SearchUiResult) {
                    History(it.name, System.currentTimeMillis()).saveOrUpdate("name = ?", it.name)
                    _searchSuggestState.value = emptyList()
                    return@collect
                }
                shareFlow.emit(it.name)
            }
        }
    }

    val searchKeyWord
        get() = searchFlow.value.name

    val isFirstSearch get() = searchKeyWord.isEmpty()

    fun changeSearchState(state: SearchUiState) {
        _searchUiState.value = state
    }

    suspend fun getMovieList() {
        _searchMovieState.value =
            MovieRepository.getMovieList().getOrDefault(emptyList())
    }

    fun search(name: String) {
        searchFlow.value.name = name
    }

    fun verify(code: String) {
        searchFlow.value.verifyCode = code
    }

    fun refresh(movie: MovieResponse.Movie) {
        searchFlow.value.apply {
            page = 1
            movieId = movie.id
            searchUrl = movie.fullSearchUrl
            parseId = movie.parseId
            verifyCode = null
            verifyUrl = movie.verifyUrl
        }
    }

    fun refresh() {
        Log.d("jdy", "change")
        searchFlow.value.page = 1
        searchFlow.value = SearchParam(searchFlow.value)
    }

    fun loadMore() {
        Log.d("jdy", "loadMore")
        searchFlow.value.page++
        searchFlow.value = SearchParam(searchFlow.value)
    }
}
