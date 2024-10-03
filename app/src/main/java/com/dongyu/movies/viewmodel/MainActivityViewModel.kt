package com.dongyu.movies.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.model.update.Update
import com.dongyu.movies.network.AppRepository
import com.dongyu.movies.network.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {

    private val _updateState = MutableStateFlow<Result<Update?>?>(null)
    val updateState: StateFlow<Result<Update?>?> get() = _updateState

    suspend fun movieListState() = MovieRepository.getHomeMoviesList()

    init {
        viewModelScope.launch {
            _updateState.value = AppRepository.checkUpdate()
        }
    }
}