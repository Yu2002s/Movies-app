package com.dongyu.movies.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.model.movie.MovieDetail
import com.dongyu.movies.model.movie.MovieResponse
import com.dongyu.movies.model.movie.PlayHistory
import com.dongyu.movies.model.movie.VideoSource
import com.dongyu.movies.model.parser.ParseParam
import com.dongyu.movies.model.search.IQiYiSearchParams
import com.dongyu.movies.network.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.litepal.LitePal
import org.litepal.extension.findFirst

class VideoViewModelFactory(private val parseParam: ParseParam?) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VideoViewModel(parseParam) as T
    }
}

/**
 * 视频播放VM
 */
class VideoViewModel(private val parseParam: ParseParam?) : ViewModel() {

    /**
     * 视频播放历史状态流
     */
    private val _playHistoryStateFlow = MutableStateFlow<PlayHistory?>(null)
    val playHistoryStateFlow = _playHistoryStateFlow.asStateFlow()

    /**
     * 详情数据状态流
     */
    private val _detailStateFlow = MutableStateFlow<Result<MovieDetail>?>(null)

    val detailStateFlow = _detailStateFlow.asStateFlow()

    /**
     * 视频播放状态流
     */
    private val _videoStateFlow = MutableStateFlow<Result<String>?>(null)
    val videoStateFlow = _videoStateFlow.asStateFlow()

    /**
     * 弹幕状态流
     */
    private val _danmakuStateFlow = MutableStateFlow(emptyList<String>())
    val danmakuStateFlow = _danmakuStateFlow.asStateFlow()

    init {
        if (parseParam != null) {
            viewModelScope.launch(Dispatchers.IO) {
                _playHistoryStateFlow.value = LitePal.where(
                    "detailId = ? and movieId = ?",
                    parseParam.detailId,
                    parseParam.movieId.toString()
                )
                    .limit(1)
                    .findFirst<PlayHistory>()?.also {
                        parseParam.selection = it.selection
                    } ?: PlayHistory(
                    movieId = parseParam.movieId,
                    detailId = parseParam.detailId
                )
                refresh()
            }
        }
    }

    private var _currentMovie: MovieResponse.Movie? = null

    val currentMovie get() = _currentMovie!!

    /**
     * 重新加载详情内容
     */
    fun refresh() {
        if (parseParam == null) {
            return
        }
        _videoStateFlow.value = null
        _detailStateFlow.value = null
        viewModelScope.launch {
            // 这里获取基本信息
            val parseUrl = parseParam.parseUrl
            val parseId = parseParam.parseId

            // 获取当前播放的影视信息
            if (_currentMovie == null) {
                _currentMovie = MovieRepository.getMovieById(parseParam.movieId).getOrNull()
            }

            // 没有获取到具体信息直接异常返回
            if (_currentMovie == null) {
                _detailStateFlow.value = Result.failure(Throwable("获取数据失败！请重试"))
                return@launch
            }

            if (parseUrl == null || parseId == -1) {
                parseParam.parseId = currentMovie.parseId
                parseParam.parseUrl = currentMovie.fullDetailUrl
            }

            // 先获取到影视基本信息
            MovieRepository.getMovieDetail(parseParam).collect { result ->
                // 当获取详情成功之后，立即进行获取视频播放地址并进行播放
                result.onSuccess { detail ->
                    // 从历史记录中
                    updateDetail(detail)
                    getDanmakuUrls(detail)
                }
                _detailStateFlow.value = result
            }
        }
    }

    /**
     * 获取弹幕链接地址集合
     */
    private suspend fun getDanmakuUrls(detail: MovieDetail) {
        MovieRepository.getMovieDanMuKu(
            IQiYiSearchParams(
                detail.movieItem.tvName,
                detail.movieItem.years.toIntOrNull() ?: 0,
                playHistoryStateFlow.value?.selection ?: 1
            )
        ).collect { urls ->
            _danmakuStateFlow.value = urls
        }
    }

    fun getDanmakuUrls(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            MovieRepository.getMovieDanMuKu(
                IQiYiSearchParams(name = name)
            ).collect { urls ->
                _danmakuStateFlow.value = urls
            }
        }
    }

    fun updateHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            playHistoryStateFlow.value?.save()
        }
    }

    /**
     * 更新详情信息
     */
    private fun updateDetail(detail: MovieDetail) {
        playHistoryStateFlow.value?.apply {
            // 影视名称
            name = detail.movieItem.tvName
            // 影视封面
            cover = detail.movieItem.cover
            // 设置默认的集数数量
            if (detail.videoSources.isNotEmpty()) {
                totalSelection = detail.videoSources[0].items.size
            }

            // 从历史记录中获取，替换默认的线路
            detail.videoSources.find { it.id == this.sourceId }
                ?.items?.find { it.param.selectionId == this.selectionId }?.let {
                    detail.currentSourceItem = it
                }

            detail.videoSources.forEach {
                it.items.forEach { item ->
                    item.param.apply {
                        videoUrl = currentMovie.fullVideoUrl
                        parseId = currentMovie.parseId
                    }
                }
            }
        }
    }

    private fun saveOrUpdateVideo(sourceItem: VideoSource.Item) {
        val name = detailStateFlow.value?.getOrNull()?.movieItem?.tvName ?: ""
        val playParam = sourceItem.param
        // 视频页解析地址
        // playParam.videoUrl = currentMovie.fullVideoUrl
        // 解析id
        // playParam.parseId = currentMovie.parseId
        playHistoryStateFlow.value?.apply {
            if (selection != sourceItem.index) {
                currentTime = 0
                Log.d("VideoActivity", "resetCurrent")
            }
            this.name = name + " " + sourceItem.name
            // 源id
            sourceId = playParam.sourceId
            // 集数id
            selectionId = playParam.selectionId
            // 最后播放时间
            lastPlayTime = System.currentTimeMillis()
            // 集数位置
            selection = sourceItem.index
            save()
        }
    }

    fun clearVideo() {
        _videoStateFlow.value = null
    }

    fun rePlay(sourceItem: VideoSource.Item) {
        _videoStateFlow.value = null
        play(sourceItem)
    }

    /**
     * 开始播放（获取播放视频并进行播放）
     */
    fun play(sourceItem: VideoSource.Item) {
        if (_currentMovie == null) {
            return
        }

        // 详情获取成功时，在进行获取视频播放地址
        viewModelScope.launch(Dispatchers.IO) {
            saveOrUpdateVideo(sourceItem)
            MovieRepository.getMovieVideo(sourceItem.param).collect { result ->
                _videoStateFlow.value = result
            }
        }
    }

}