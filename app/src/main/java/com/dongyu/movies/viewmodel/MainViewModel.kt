package com.dongyu.movies.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.data.home.MainData
import com.dongyu.movies.network.HomeRepository
import com.dongyu.movies.utils.SpUtils.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _bannerState = MutableStateFlow(5000)
    val bannerState: StateFlow<Int> = _bannerState

    private val _stateFlow = MutableStateFlow<Result<MainData>?>(null)
    val stateFlow: StateFlow<Result<MainData>?> get() = _stateFlow

    init {
        refresh()

        viewModelScope.launch {
            while (true) {
                _bannerState.value++
                delay(5000)
                if (bannerState.value >= 10000) {
                    _bannerState.value = 5000
                }
            }
        }
    }

    fun getCurrentItem() = bannerState.value

    fun getMainData() = _stateFlow.value?.getOrNull()

    fun getCurrentRouteId() = getMainData()?.movieId

    fun refresh() {
        viewModelScope.launch {
            HomeRepository.getMain().collect {
                _stateFlow.value = it
                it.onSuccess { data ->
                    SPConfig.CURRENT_ROUTE_ID put data.movieId
                }
            }
        }
    }
}