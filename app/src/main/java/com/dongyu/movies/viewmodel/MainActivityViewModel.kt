package com.dongyu.movies.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.data.movie.MovieResponse
import com.dongyu.movies.data.search.History
import com.dongyu.movies.data.search.SearchSuggestItem
import com.dongyu.movies.data.search.SearchUiResult
import com.dongyu.movies.data.search.SearchUiState
import com.dongyu.movies.data.search.SearchUiSuggest
import com.dongyu.movies.data.update.Update
import com.dongyu.movies.network.AppRepository
import com.dongyu.movies.network.HomeRepository
import com.dongyu.movies.network.SearchRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {

    private val _updateState = MutableStateFlow<Result<Update?>?>(null)
    val updateState: StateFlow<Result<Update?>?> get() = _updateState

    suspend fun movieListState() = HomeRepository.getHomeMoviesList()

    // 搜索UI状态，默认未搜索历史界面
    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiSuggest())

    val searchUiState: StateFlow<SearchUiState> = _searchUiState

    private val _searchSuggestState = MutableStateFlow<List<SearchSuggestItem>>(emptyList())
    val searchSuggestState: StateFlow<List<SearchSuggestItem>> get() = _searchSuggestState

    private val shareFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        viewModelScope.launch {
            shareFlow.collectLatest { name ->
                // 此处发起网络请求
                _searchSuggestState.value =
                    SearchRepository.getSearchSuggest(name).single().getOrDefault(emptyList())
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

    fun changeSearchState(state: SearchUiState) {
        _searchUiState.value = state
    }

    init {
        viewModelScope.launch {
            _updateState.value = AppRepository.checkUpdate()
        }
    }
}