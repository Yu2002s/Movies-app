package com.dongyu.movies.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.R
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.databinding.ActivityVideoBinding
import com.dongyu.movies.databinding.ItemGridMoviesBinding
import com.dongyu.movies.databinding.ItemListSourceBinding
import com.dongyu.movies.databinding.LayoutPlayerSettingBinding
import com.dongyu.movies.dialog.BaseAppCompatDialog
import com.dongyu.movies.dialog.MovieDetailDialog
import com.dongyu.movies.dialog.SearchMovieDialog
import com.dongyu.movies.event.OnSourceItemChangeListener
import com.dongyu.movies.event.OnVideoErrorBtnClickListener
import com.dongyu.movies.model.movie.BaseMovieItem
import com.dongyu.movies.model.movie.MovieDetail
import com.dongyu.movies.model.movie.MovieItem
import com.dongyu.movies.model.movie.MovieVideo
import com.dongyu.movies.model.movie.PlayHistory
import com.dongyu.movies.model.movie.VideoSource
import com.dongyu.movies.model.movie.VideoType
import com.dongyu.movies.model.movie.loadListCardMovies
import com.dongyu.movies.model.parser.ParseParam
import com.dongyu.movies.model.parser.PlayParam
import com.dongyu.movies.utils.SpUtils
import com.dongyu.movies.utils.SpUtils.get
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.view.player.DongYuPlayer
import com.dongyu.movies.view.player.base.BasePlayer
import com.dongyu.movies.view.player.base.PlayerStateListener
import com.dongyu.movies.viewmodel.VideoViewModel
import com.dongyu.movies.viewmodel.VideoViewModelFactory
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wanban.screencast.Utils
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * 视频播放页面
 */
class VideoActivity : BaseActivity(), PlayerStateListener, OnSourceItemChangeListener {

    private val binding by lazy {
        ActivityVideoBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<VideoViewModel> {
        VideoViewModelFactory(parseParam)
    }

    private lateinit var videoSources: List<VideoSource>
    private lateinit var tvName: String
    private lateinit var movieItem: MovieItem

    private val searchMovieDialog by lazy {
        SearchMovieDialog(this, tvName)
    }

    companion object {

        private val TAG = VideoActivity::class.java.simpleName

        private const val PARAM_URL = "url"

        private const val PARAM_TYPE = "type"

        private const val PARAM_PARSE = "parse_param"

        private const val PARAM_NAME = "name"

        /**
         * 播放视频
         * @param videoType 视频类型
         * @param url 播放地址
         */
        fun play(videoType: VideoType, url: String, name: String) {
            if (videoType == VideoType.PARSE) {
                Log.e(TAG, "无法使用解析类型")
                return
            }
            val context = MoviesApplication.context
            val intent = Intent(context, VideoActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(PARAM_TYPE, videoType)
                putExtra(PARAM_URL, url)
                putExtra(PARAM_NAME, name)
            }
            context.startActivity(intent)
        }

        /**
         * 解析播放
         * @param param 解析所需的参数
         */
        fun play(param: ParseParam) {
            val context = MoviesApplication.context
            val intent = Intent(context, VideoActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(PARAM_TYPE, VideoType.PARSE)
                putExtra(PARAM_NAME, param.tvName)
                putExtra(PARAM_PARSE, param)
            }
            context.startActivity(intent)
        }
    }

    /**
     * 播放所需的解析参数，必须传递，否则报错
     */
    private val parseParam by lazy {
        (intent.getParcelableExtra<ParseParam>(PARAM_PARSE))?.also {
            tvName = it.tvName
            binding.tvName.text = tvName
        }
    }

    private var _playHistory: PlayHistory? = null

    private val playHistory get() = _playHistory!!

    private val videoType by lazy {
        intent.getSerializableExtra(PARAM_TYPE) as VideoType
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fixDanmaku()
        setContentView(binding.root)

        init()
        initView()
        initEvent()
        collectFlow()
    }

    private fun collectFlow() {
        lifecycleScope.launch {
            viewModel.detailStateFlow.collect { result ->
                updateDetail(result)
            }
        }

        lifecycleScope.launch {
            viewModel.playHistoryStateFlow.collect { history ->
                _playHistory = history
                updateCollectIcon()
            }
        }

        lifecycleScope.launch {
            viewModel.videoStateFlow.collect { result ->
                playVideo(result)
            }
        }

        lifecycleScope.launch {
            viewModel.danmakuStateFlow.collect {
                if (it.isEmpty()) {
                    return@collect
                }
                binding.dyPlayer.setDanmakus(it)
            }
        }
    }

    /**
     * 更新详情内容
     */
    private fun updateDetail(result: Result<MovieDetail>?) {
        val loading = binding.loading
        val error = binding.error.root
        if (result == null) {
            loading.isVisible = true
            error.isVisible = false
            return
        }
        loading.isVisible = false
        binding.sourceView.isInvisible = false
        result.onSuccess { data ->
            Log.d(TAG, "detail: $data")
            error.isVisible = false
            movieItem = data.movieItem
            tvName = movieItem.tvName
            videoSources = data.videoSources
            binding.dyPlayer.setVideoSources(data.videoSources)
            binding.apply {
                tvName.text = this@VideoActivity.tvName
                tvDesc.text =
                    movieItem.director + " " + movieItem.years + " " + movieItem.type + " 详情>"
                // 判断video是否为空，不为空则表示详情附带视频信息
                sourceView.submitList(data.videoSources, false, this@VideoActivity)
                    .setSelection(data.currentSourceItem)
                recommendRv.models = data.recommendMovies
                /*if (data.recommendMovies.isNullOrEmpty()) {
                    binding.state.showEmpty()
                } else {
                    binding.state.showContent()
                }*/
            }
        }.onFailure {
            binding.dyPlayer.hideLoading()
            Log.e(TAG, it.toString())
            error.isVisible = true
            showSearchFragmentDialog()
            // binding.state.showError()
        }
    }

    /**
     * 播放视频
     */
    private fun playVideo(result: Result<MovieVideo>?) {
        if (result == null) {
            return
        }
        val dyPlayer = binding.dyPlayer
        result.onSuccess { video ->
            if (!dyPlayer.isScreencast) {
                dyPlayer.hideMaskView()
            }
            val sourceView = binding.sourceView
            videoSources.getOrNull(sourceView.currentSourcePosition)?.items?.forEachIndexed { index, item ->
                video.urls?.getOrNull(index)?.let {
                    item.url = it
                }
                if (sourceView.currentSourceItemPosition == index) {
                    item.url?.let {
                        video.url = it
                    }
                }
            }
            dyPlayer.setHeaders(video.headers).play(video.url)
        }.onFailure { e ->
            if (e is CancellationException) {
                // 当请求被取消时
                return@onFailure
            }
            // 自动切换线路
            val autoSwitchRoute = SpUtils.DEFAULT_KEY.get(SPConfig.PLAYER_AUTO_SWITCH_ROUTE, true)!!
            if (!(autoSwitchRoute && binding.sourceView.switchSource())) {
                dyPlayer.apply {
                    hideLoading()
                    playError()
                    showMaskView()
                }
                (e.message + "\n请尝试更换线路、换源").showToast()
            } else {
                "播放失败，切换线路中...".showToast()
            }
            Log.e(TAG, e.toString())
        }
    }

    private fun updateCollectIcon() {
        _playHistory ?: return
        val collectIcon =
            if (playHistory.isCollected) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24
        binding.collect.setImageResource(collectIcon)
    }

    /**
     * 搜索对话框
     */
    private fun showSearchFragmentDialog() {
        if (!::tvName.isInitialized) {
            return
        }
        if (tvName.isBlank()) {
            "详情未加载，建议在搜索页进行搜索其他源播放".showToast()
            return
        }
        if (searchMovieDialog.isShowing) {
            return
        }
        searchMovieDialog.show()
    }

    private fun showPlayerSettingDialog() {
        val playerSetting = LayoutPlayerSettingBinding.inflate(layoutInflater).root
        if (binding.dyPlayer.isFullScreen) {
            BaseAppCompatDialog(this).apply {
                setContentView(playerSetting)
                setOnDismissListener {
                    playerSetting.removeAllViewsInLayout()
                }
                show()
            }
            return
        }
        BottomSheetDialog(this).apply {
            setContentView(playerSetting)
            setOnDismissListener {
                playerSetting.removeAllViewsInLayout()
            }
            show()
        }
    }

    /**
     * 初始化
     */
    private fun init() {
        /*val dyPlayer = binding.dyPlayer
        dyPlayer.apply {
            play(videoUrl)
            showToast(getString(R.string.play_toast))
        }*/
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        if (videoType != VideoType.PARSE) {
            binding.scrollview.isVisible = false
            binding.dyPlayer.isFullScreen = true
            // 加载弹幕
            intent.getStringExtra(PARAM_NAME)?.let {
                binding.dyPlayer.title = it
                viewModel.getDanmakuUrls(it)
            }
            intent.getStringExtra(PARAM_URL)?.let {
                binding.dyPlayer.play(it)
            }
            return
        }
        binding.recommendRv.loadListCardMovies {
            val movieId = parseParam?.movieId ?: return@loadListCardMovies
            play(ParseParam(movieId = movieId, detailId = it.id, tvName = it.tvName))
        }
    }

    /**
     * 页面事件处理
     */
    private fun initEvent() {
        val dyPlayer = binding.dyPlayer

        if (videoType == VideoType.PARSE) {
            // 详情加载错误点击刷新
            binding.error.root.setOnClickListener {
                it.isInvisible = true
                binding.loading.isInvisible = false
                viewModel.refresh()
            }

            // 播放器错误按钮处理点击事件
            dyPlayer.setErrorBtnClickListener(object : OnVideoErrorBtnClickListener {

                override fun onRefreshClick() {
                    // 仅进行重新播放
                    dyPlayer.refresh()
                    dyPlayer.hideMaskView()
                }

                override fun onReloadClick(currentSourceItem: VideoSource.Item) {
                    dyPlayer.hideMaskView()
                    dyPlayer.showLoading()
                    // 清除选中的源
                    binding.sourceView.clearSelection()
                    // 重新播放，获取播放信息
                    viewModel.play(currentSourceItem)
                }

                override fun onSwitchSourceClick() {
                    // 换源
                    if (!binding.sourceView.switchSource()) {
                        showSearchFragmentDialog()
                    }
                }
            })

            // 换源按钮点击事件
            binding.switchSource.setOnClickListener {
                if (!::tvName.isInitialized || tvName.isBlank()) {
                    "建议在搜索页搜索其他源进行播放，已为您跳转".showToast()
                    startActivity(Intent(this, SearchActivity::class.java))
                    return@setOnClickListener
                }
                showSearchFragmentDialog()
            }

            // 监听视频播放状态
            dyPlayer.setPlayerStateListener(this)

            // 监听播放器选集改变事件
            dyPlayer.setSourceItemChangeListener { item, _ ->
                binding.sourceView.setSelection(item)
            }

            // 视频下载按钮点击事件
            dyPlayer.headerBinding.videoDownload.setOnClickListener {
                if (!::videoSources.isInitialized || videoSources.isEmpty()) {
                    "线路列表为空或未加载，请等待加载后尝试".showToast()
                    return@setOnClickListener
                }
                VideoSourceActivity.start(
                    videoSources,
                    binding.sourceView.currentSourceItemPosition,
                    movieItem.tvName
                )
            }

            // 详情点击事件，查看视频详细信息
            binding.tvDesc.setOnClickListener {
                if (!::movieItem.isInitialized) {
                    return@setOnClickListener
                }
                MovieDetailDialog(this, movieItem).show()
            }

            // 收藏按钮的点击事件
            binding.collect.setOnClickListener {
                playHistory.isCollected = !playHistory.isCollected
                updateCollectIcon()
                viewModel.updateHistory()
            }
        }

        // 播放设置点击事件
        dyPlayer.headerBinding.videoSetting.setOnClickListener {
            showPlayerSettingDialog()
        }

        // 弹幕显示状态点击事件
        dyPlayer.bottomBinding.danmakuVisible.setOnClickListener { v ->
            val textView = v as TextView
            if (textView.text == "弹幕开") {
                dyPlayer.hideDanmaku()
            } else {
                val sourcePosition = binding.sourceView.currentSourceItemPosition
                if (sourcePosition == -1) {
                    return@setOnClickListener
                }
                if (!dyPlayer.showDanmaku()) {
                    "弹幕未加载".showToast()
                } else {
                    dyPlayer.startDanmaku(sourcePosition)
                }
            }
        }
    }

    /**
     * 当源被改变时被调用
     */
    override fun onSourceItemChanged(item: VideoSource.Item, position: Int) {
        Log.d(TAG, "onSourceItemChanged: $item")
        binding.dyPlayer.apply {
            setCurrentSourceItem(item)
            hideMaskView()
            showLoading()
            if (!isScreencast) {
                stop()
                title = tvName + " " + item.name
                startDanmaku(position)
            }
        }
        viewModel.play(item)
    }

    /**
     * 当视频已经准备播放时
     */
    override fun onVideoPrepared(player: BasePlayer) {
        super.onVideoPrepared(player)
        player as DongYuPlayer
        // 自动全屏
        val autoFullscreen: Boolean = SPConfig.PLAYER_AUTO_FULLSCREEN.get<Boolean>(false)!!
        if (autoFullscreen) {
            player.isFullScreen = true
        }
        val currentTime = playHistory.currentTime
        Log.d(TAG, "seekTime: $currentTime")
        if (currentTime == 0L) {
            skipVideoStart()
        } else {
            player.seekTo(currentTime)
        }
    }

    override fun onProgressChanged(currentProgress: Long) {
        super.onProgressChanged(currentProgress)
        var duration = playHistory.duration
        val player = binding.dyPlayer

        if (duration == 0L) {
            duration = player.endProgress
            playHistory.duration = player.endProgress
        }

        if (!player.isSeeking) {
            skipVideoEnd()
            playHistory.currentTime = currentProgress
            playHistory.progress = (currentProgress * 100 / duration).toInt()
            viewModel.updateHistory()
        }
    }

    override fun onPlayStateChanged(newState: Int) {
        super.onPlayStateChanged(newState)
        if (newState == BasePlayer.STATE_COMPLETED) {
            // 是否自动播放下一集
            val autoNextSelection = SPConfig.PLAYER_AUTO_NEXT.get<Boolean>(true)!!
            val hasNextSelection = playHistory.hasNextSelection
            if (!hasNextSelection) {
                playHistory.apply {
                    progress = 100
                    currentTime = duration
                }
            } else if (!autoNextSelection) {
                playHistory.apply {
                    progress = 0
                    currentTime = 0
                }
            }
            viewModel.updateHistory()
            if (binding.sourceView.isLastSourceItem) {
                binding.dyPlayer.showToast("已全部播放完成，感谢观看！")
            }

            if (autoNextSelection) {
                if (!binding.sourceView.setNextSelection()) {
                    binding.dyPlayer.pause()
                }
            }
        } else if (newState == BasePlayer.STATE_ERROR) {
            viewModel.clearVideo()
        }
    }

    /**
     * 跳过片头
     */
    private fun skipVideoStart() {
        // 跳过片头
        val skipStart = SPConfig.PLAYER_SKIP_START.get<Boolean>(false)!!
        if (!skipStart) {
            return
        }
        val skipTime = SPConfig.PLAYER_SKIP_START_TIME.get<Int>(0)!!
        if (skipTime != 0) {
            binding.dyPlayer.apply {
                seekTo(skipTime.toLong())
                showToast("已自动跳过片头，正在播放：${title}")
            }
        }
    }

    /**
     * 跳过片尾
     */
    private fun skipVideoEnd() {
        val duration = playHistory.duration
        val skipEnd = SPConfig.PLAYER_SKIP_END.get<Boolean>(false)!!
        if (skipEnd && binding.sourceView.hasNextSelection()) {
            val skipTime = SPConfig.PLAYER_SKIP_END_TIME.get<Long>(0L)!!
            if (skipTime != 0L && duration - binding.dyPlayer.realCurrentProgress <= skipTime) {
                binding.sourceView.setNextSelection()
                // 跳过片尾
                binding.dyPlayer.showToast("已自动跳过片尾，正在播放：${binding.dyPlayer.title}")
                return
            }
        }
    }

    /**
     * 修复弹幕滚动问题（将当前页面的刷新设置为60hz）
     */
    private fun fixDanmaku() {
        // https://github.com/bilibili/DanmakuFlameMaster/issues/445
        // 获取系统window支持的模式
        val modes = window.windowManager.defaultDisplay.supportedModes
        // 对获取的模式，基于刷新率的大小进行排序，从小到大排序
        modes.sortBy {
            it.refreshRate
        }

        window.let {
            val lp = it.attributes
            // 取出最小的那一个刷新率，直接设置给window
            lp.preferredDisplayModeId = modes.first().modeId
            it.attributes = lp
        }
    }

    override fun onInsetChanged(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
        return true
    }

    override fun getRecyclerView(): RecyclerView {
        return binding.recommendRv
    }

    override fun onResume() {
        super.onResume()
        binding.dyPlayer.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.dyPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.dyPlayer.stop()
    }
}