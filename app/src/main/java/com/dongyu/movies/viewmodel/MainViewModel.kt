package com.dongyu.movies.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.network.Repository
import com.dongyu.movies.model.home.MainData
import com.dongyu.movies.network.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _stateFlow = MutableStateFlow<Result<MainData>?>(null)
    val stateFlow: StateFlow<Result<MainData>?> get() = _stateFlow

    fun refresh() {
        viewModelScope.launch {
             val movieId = Repository.currentMovieId
            MovieRepository.getHomeMovie(movieId).collect {
                _stateFlow.value = it
            }
        }
    }
}