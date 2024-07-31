package com.dongyu.movies.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.dongyu.movies.data.history.HistoryPagingSource

class HistoryViewModel : ViewModel() {

    val historyFlow = Pager(
        config = PagingConfig(pageSize = 10)
    ) {
        HistoryPagingSource()
    }
        .flow
        .cachedIn(viewModelScope)

}