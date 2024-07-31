package com.dongyu.movies.viewmodel;

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.data.movie.PlayParam
import com.dongyu.movies.data.search.IQiYiSearchParams
import com.dongyu.movies.network.MovieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VideoViewModel : ViewModel() {

    companion object {
        private val TAG = VideoViewModel::class.java.simpleName
    }

    private val _videoState = MutableStateFlow<PlayParam?>(null)

    private val _playState = MutableStateFlow<PlayParam?>(null)

    private val _danmakuState = MutableStateFlow<IQiYiSearchParams?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val danmakuState = _danmakuState
        .filter { it != null }
        .flatMapLatest {
            Log.d("jdy", "getDanMuKu: $it")
            MovieRepository.getMovieDanMuKu(it!!)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val videoDetailState = _videoState
        .filter {
            it != null
        }
        .flatMapLatest {
            MovieRepository.getMovieDetail(it!!)
        }.onEach {
            it.getOrNull()?.let { detail ->
                _danmakuState.value = (IQiYiSearchParams(
                    name = detail.main.tvName,
                    year = detail.main.years,
                    selection = 1
                ))
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val playState = _playState
        .filter { it != null }
        .flatMapLatest {
            MovieRepository.getMovieVideo(it!!)
        }

    fun play(playParam: PlayParam) {
        viewModelScope.launch {
            _videoState.emit(playParam)
        }
    }

    fun play(routeId: Int, selection: Int) {
        _videoState.value?.let {
            _playState.value = PlayParam(
                it.id,
                it.detailId,
                routeId = routeId,
                selection = selection
            )
        }
    }

    fun refresh() {
        val value = _videoState.value ?: return
        val param = PlayParam(value.id, value.detailId, value.routeId, value.selection)
        _videoState.value = null
        _videoState.value = param
    }
}
