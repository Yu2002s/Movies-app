package com.dongyu.movies.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.dongyu.movies.data.home.ClassifyPagingSource
import com.dongyu.movies.data.home.ClassifyQueryParam
import com.dongyu.movies.data.home.FilterData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest

class ClassifyViewModelFactory(private val cateId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClassifyViewModel::class.java)) {
            return ClassifyViewModel(cateId) as T
        }
        throw IllegalArgumentException()
    }
}

class ClassifyViewModel(cateId: Int) : ViewModel() {

    private val _pageState = MutableStateFlow(ClassifyQueryParam(cateId = cateId))

    private val _filterState = MutableStateFlow<List<FilterData>>(emptyList())

    val filterState: StateFlow<List<FilterData>> get() = _filterState

    private val pageFlow = Pager(config = PagingConfig
        (pageSize = 30, initialLoadSize = 30, prefetchDistance = 12),
        pagingSourceFactory = { ClassifyPagingSource(_pageState.value, _filterState) }
    )
        .flow.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val classifyState = _pageState
        .flatMapLatest {
            pageFlow
        }

    fun getParam()  = _pageState.value

    fun refresh(param: ClassifyQueryParam) {
        _pageState.value = param
    }

    val movieId get() =  _pageState.value.currentMovieId
}