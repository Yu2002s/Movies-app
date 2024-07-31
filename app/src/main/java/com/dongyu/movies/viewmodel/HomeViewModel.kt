package com.dongyu.movies.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.data.search.History
import com.dongyu.movies.data.search.SearchSuggestItem
import com.dongyu.movies.data.search.SearchUiResult
import com.dongyu.movies.data.search.SearchUiState
import com.dongyu.movies.data.search.SearchUiSuggest
import com.dongyu.movies.network.SearchRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {
}