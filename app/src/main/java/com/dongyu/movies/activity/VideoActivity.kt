package com.dongyu.movies.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.text.buildSpannedString
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.R
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.data.history.PlayHistory
import com.dongyu.movies.data.movie.MovieDetail
import com.dongyu.movies.data.movie.PlayParam
import com.dongyu.movies.data.movie.PlaySource
import com.dongyu.movies.data.movie.Video
import com.dongyu.movies.databinding.ActivityVideoBinding
import com.dongyu.movies.databinding.DialogDownloadBinding
import com.dongyu.movies.databinding.LayoutPlayerSettingBinding
import com.dongyu.movies.dialog.SearchDialog
import com.dongyu.movies.dialog.VideoDetailDialog
import com.dongyu.movies.event.OnRouteChangeListener
import com.dongyu.movies.network.MovieRepository
import com.dongyu.movies.utils.SpUtils.get
import com.dongyu.movies.utils.ioThread
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.view.player.base.BasePlayer
import com.dongyu.movies.view.player.base.PlayerStateListener
import com.dongyu.movies.viewmodel.VideoViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.litepal.LitePal
import org.litepal.extension.findFirst


class VideoActivity : BaseActivity(), OnRouteChangeListener, PlayerStateListener {

    private val binding by lazy {
        ActivityVideoBinding.inflate(layoutInflater)
    }

    companion object {

        private const val TAG = "jdy"

        private val IDM_PACKAGE_NAME =
            arrayOf("idm.internet.download.manager", "idm.internet.download.manager.plus")

        private const val IDM_DOWNLOAD_URL = "https://www.123pan.com/s/km2hjv-Y8ROA.html"

        fun play(playParam: PlayParam) {
            val intent = Intent(MoviesApplication.context, VideoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("params", playParam)
            MoviesApplication.context.startActivity(intent)
        }
    }

    private val videoViewModel by viewModels<VideoViewModel>()

    private var movieDetail: MovieDetail? = null
    private var movieId: Int = -1

    private val searchDialog by lazy {
        SearchDialog(this, movieDetail?.main?.tvName)
    }

    private var _playHistory: PlayHistory? = null

    private val playHistory get() = _playHistory!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        collectData()
        initEvent()
    }

    private fun downloadVideo(routeId: Int, selection: Int, selectionName: String) {
        if (movieDetail == null) {
            return
        }

        val downloadTips = fun() {
            MaterialAlertDialogBuilder(this).apply {
                setTitle("需要外部下载器")
                setMessage("请下载IDM+，并安装到手机中。\n注意：安装完成后请先打开IDM进行初始化，如设置中文和存储目录")
                setPositiveButton("去下载") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(IDM_DOWNLOAD_URL))
                    startActivity(intent)
                }
                setNegativeButton("取消", null)
                show()
            }
        }

        val startDownload = fun(video: Video) {
            var count = 0
            IDM_PACKAGE_NAME.forEach { packageName ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.url))
                    intent.putExtra("filename", "${movieDetail!!.main.tvName}-${selectionName}.mp4")
                    intent.`package` = packageName
                    startActivity(intent)
                    return@forEach
                } catch (e: Exception) {
                    e.printStackTrace()
                    count++
                }
            }
            if (count == IDM_PACKAGE_NAME.size) {
                downloadTips()
            }
        }

        lifecycleScope.launch {
            MovieRepository.getMovieVideo(
                PlayParam(
                    movieId,
                    movieDetail!!.main.id,
                    routeId,
                    selection
                )
            ).collect {
                it.onSuccess { video ->
                    startDownload(video)
                }.onFailure { exception: Throwable ->
                    exception.message.showToast()
                }
            }
        }
    }

    private fun init() {
        binding.dyPlayer.showToast(getString(R.string.play_toast))
        IntentCompat.getParcelableExtra(intent, "params", PlayParam::class.java)?.let {
            ioThread {
                _playHistory =
                    LitePal.where("movieId = ? and detailId = ?", it.id.toString(), it.detailId)
                        .findFirst<PlayHistory>()

                if (_playHistory == null) {
                    // 没有保存历史记录
                    _playHistory = PlayHistory(
                        // 复制当前的影视id
                        movieId = it.id,
                        // 当前页面的详情id
                        detailId = it.detailId,
                        // 当前播放的路线id
                        routeId = it.routeId
                    )
                } else {
                    // 保存了历史记录
                    // 把保存的路线id赋值给当前播放器
                    playHistory.routeId?.let { routeId -> it.routeId = routeId }
                    // 对保存的集数进行复制
                    it.selection = (playHistory.selection ?: 1)
                }
                movieId = it.id
                // 获取详情数据
                videoViewModel.play(it)
            }
        }
    }

    private fun collectData() {
        val dyPlayer = binding.dyPlayer

        lifecycleScope.launch {
            videoViewModel.videoDetailState.collect {
                binding.loading.isInvisible = true
                it.onSuccess { detail ->
                    binding.refreshBtn.isInvisible = true
                    movieDetail = detail
                    dyPlayer.play(detail.url?.url)
                    if (playHistory.current != 0L) {
                        dyPlayer.endProgress = playHistory.duration
                        dyPlayer.setProgress(playHistory.current)
                    }
                    // 这里获取到了具体的播放数据
                    updateDetailView(detail)

                    if (detail.url == null) {
                        // 解析失败的情况，切换解析线路d
                        // binding.routeView.switchRoute()
                        "解析失败，请尝试手动切换线路".showToast()
                    } else {
                        // 设置播放地址信息
                        playHistory.video = detail.url!!
                    }
                    playHistory.cover = detail.main.cover

                    if (movieId != -1) {
                        ioThread {
                            playHistory.saveOrUpdate(
                                "movieId = ? and detailId = ?", movieId.toString(), detail.main.id
                            )
                        }
                    }
                }.onFailure { exception: Throwable ->
                    Log.e(TAG, exception.toString())
                    binding.refreshBtn.isInvisible = false
                    exception.message.showToast()
                }
            }
        }

        lifecycleScope.launch {
            videoViewModel.playState.collect {
                dyPlayer.hideMessage()
                dyPlayer.hideLoading()
                it.onSuccess { video ->
                    dyPlayer.hideMaskView()
                    movieDetail?.url = video
                    if (video.url == null) {
                        // binding.routeView.switchRoute()
                        "请尝试手动切换线路".showToast()
                        return@collect
                    }
                    dyPlayer.play(video.url)
                    playHistory.video = video
                    playHistory.totalSelection = binding.routeView.sourcesCount
                    binding.routeView.restRetryCount()
                    updatePlayHistory()
                }.onFailure { exception: Throwable ->
                    Log.e(TAG, exception.toString())
                    if (exception is CancellationException) {
                        // 当请求被取消时
                        return@onFailure
                    }
                    // binding.routeView.switchRoute()
                    "解析失败，请尝试手动切换线路".showToast()

                    dyPlayer.onPlayStateChanged(BasePlayer.STATE_ERROR)
                }
            }
        }

        lifecycleScope.launch {
            videoViewModel.danmakuState.collect {
                Log.d(TAG, "danmukuUrl: $it")
                dyPlayer.setDanmakus(it).startDanmaku(binding.routeView.currentSelectionPosition + 1)
            }
        }
    }

    /**
     * 线路发生改变时调用
     */
    private fun routeChange() {
        setVideoTitle()
        binding.dyPlayer.apply {
            stop()
            hideMaskView()
            showLoading()
            startDanmaku(playHistory.selection ?: 1)
        }
        updatePlayHistory()
    }

    /**
     * 监听集数发生改变时
     */
    override fun onSelectionChanged(routeId: Int, currentRoute: Int, currentSelection: Int) {
        Log.i(TAG, "onRouteChanged, routeId: $routeId, selection: $currentSelection")
        videoViewModel.play(routeId, currentSelection)
        if (playHistory.routeId == routeId || (playHistory.selection != currentSelection)) {
            playHistory.current = 0
        }
        playHistory.routeId = routeId
        playHistory.selection = currentSelection
        routeChange()
    }

    /**
     * 下一集时调用。
     */
    override fun onNextSelection(routeId: Int, currentRoute: Int, selection: Int) {
        Log.i(TAG, "onNextSelection, routeId: $routeId, selection: $selection")
        val next = movieDetail?.url?.next
        playHistory.selection = selection
        playHistory.current = 0
        routeChange()

        if (next == null) {
            videoViewModel.play(routeId, selection)
        } else {
            Log.i(TAG, "nextVideoUrl: $next")
            binding.dyPlayer.play(next)
            // 将next置空，防止视频播放失败，持续播放下一集。
            movieDetail!!.url!!.next = null
        }
    }

    private fun initEvent() {
        val dyPlayer = binding.dyPlayer
        val routeView = binding.routeView
        val headerBinding = dyPlayer.headerBinding
        val bottomBinding = dyPlayer.bottomBinding

        dyPlayer.setPlayerStateListener(this)
        routeView.setRouteChangeListener(this)

        headerBinding.videoSetting.setOnClickListener {
            // 设置点击事件

            val playerSetting = LayoutPlayerSettingBinding.inflate(layoutInflater).root

            if (dyPlayer.isFullScreen) {
                val playerHeight: Int = dyPlayer.height
                val popupWidth: Int = (dyPlayer.width / 2.7).toInt()
                val dialog = AppCompatDialog(this)
                dialog.setContentView(playerSetting)
                dialog.setCanceledOnTouchOutside(true)
                dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                // dialog.window?.decorView?.setBackgroundResource(R.drawable.bg_corner)
                dialog.window?.attributes?.apply {
                    gravity = Gravity.END
                    width = popupWidth
                    height = playerHeight
                    dimAmount = 0.1f
                    windowAnimations = R.style.PopupWindowSlideAnim
                }
                dialog.setOnShowListener {
                    dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                    WindowCompat.getInsetsController(dialog.window!!, dialog.window!!.decorView)
                        .hide(WindowInsetsCompat.Type.navigationBars())
                }
                dialog.setOnDismissListener {
                    playerSetting.removeAllViewsInLayout()
                }
                dialog.show()
            } else {
                BottomSheetDialog(this).apply {
                    // behavior.peekHeight = getWindowHeight() / 2
                    setContentView(playerSetting)
                    setOnDismissListener {
                        playerSetting.removeAllViewsInLayout()
                    }
                    show()
                }
            }
        }

        // 选集按钮
        bottomBinding.selections.setOnClickListener {
            if (routeView.playSources.isEmpty()) {
                return@setOnClickListener
            }

            val popupWidth: Int = (dyPlayer.width / 2.7).toInt()
            val playerHeight: Int = dyPlayer.height
            val cloneRouteView = routeView.cloneView(this)
            val dialog = AppCompatDialog(this)
            dialog.setContentView(cloneRouteView)
            dialog.setCanceledOnTouchOutside(true)
            dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            dialog.window?.attributes?.apply {
                gravity = Gravity.END
                width = popupWidth
                height = playerHeight
                dimAmount = 0.1f
                windowAnimations = R.style.PopupWindowSlideAnim
            }
            dialog.setOnShowListener {
                dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                WindowCompat.getInsetsController(dialog.window!!, dialog.window!!.decorView)
                    .hide(WindowInsetsCompat.Type.navigationBars())
            }
            dialog.show()

        }

        // 下一集按钮
        bottomBinding.playNext.setOnClickListener {
            binding.routeView.nextSelection()
        }

        binding.movieInfo.setOnClickListener {
            val movieItem = movieDetail?.main ?: return@setOnClickListener
            VideoDetailDialog.newInstance(movieItem).show(supportFragmentManager, null)
        }

        binding.searchMore.setOnClickListener {
            searchDialog.show()
        }

        binding.refreshBtn.setOnClickListener {
            binding.loading.isInvisible = false
            binding.refreshBtn.isInvisible = true
            videoViewModel.refresh()
        }

        headerBinding.videoDownload.setOnClickListener {
            val binding = DialogDownloadBinding.inflate(layoutInflater)
            binding.routeView.apply {
                setAllowRepeatedSelection(true)
                playSources = movieDetail?.sources ?: emptyList<PlaySource>()
                setRouteChangeListener(object : OnRouteChangeListener {
                    override fun onNextSelection(routeId: Int, currentRoute: Int, selection: Int) {
                        downloadVideo(routeId, selection, selectionName)
                    }

                    override fun onSelectionChanged(
                        routeId: Int,
                        currentRoute: Int,
                        currentSelection: Int
                    ) {
                        downloadVideo(routeId, currentSelection, selectionName)
                    }
                })
            }
            BottomSheetDialog(this)
                .apply {
                    behavior.peekHeight = 600
                    setTitle("下载列表")
                    setContentView(binding.root)
                    show()
                }
        }

        // TODO: 收藏功能待实现
        /*binding.btnLike.setOnClickListener {
            ioThread {

            }
        }*/

        bottomBinding.danmakuVisible.setOnClickListener(View.OnClickListener {
            if (dyPlayer.isPreparedDanmaku && dyPlayer.isShowDanmaku) {
                dyPlayer.hideDanmaku()
            } else {
                dyPlayer.showManmaku()
                if (!dyPlayer.isPreparedDanmaku) {
                    dyPlayer.startDanmaku(routeView.selection)
                }
            }
        })
    }

    private fun updatePlayHistory() {
        ioThread {
            playHistory.updateAt = System.currentTimeMillis()
            playHistory.update()
        }
    }

    private fun updateDetailView(detail: MovieDetail) {
        val typeValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typeValue, true)
        val color = ContextCompat.getColor(this, typeValue.resourceId)
        binding.apply {
            val main = detail.main
            movieName.text = main.tvName
            movieInfo.text = buildSpannedString {
                append(main.area, " ", main.type, " ", main.years.toString(), " ")
                val colorSpan = ForegroundColorSpan(color)
                append("详情>", colorSpan, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        setSources(detail)
        setVideoTitle()
    }

    private fun setVideoTitle() {
        val title = movieDetail?.main?.tvName + " " + binding.routeView.selectionName
        binding.dyPlayer.title = title
        // 更新名称
        playHistory.name = title
    }

    private fun setSources(detail: MovieDetail) {
        if (detail.sources.isEmpty())
            return
        binding.routeView.apply {
            val sources = detail.sources
            val firstSource = sources[0]
            setPlaySources(sources)
            routeId = if (playHistory.selection == null || playHistory.totalSelection == 0) {
                firstSource.routeId
            } else {
                playHistory.routeId ?: firstSource.routeId
            }
            playHistory.totalSelection = sourcesCount
            val sourceData = firstSource.data
            if (sources.isEmpty()) {
                return
            }
            setSelection(playHistory.selection ?: sourceData[0].selection)
        }
    }

    override fun onVideoPrepared(player: BasePlayer) {
        playHistory.duration = player.endProgress
        updatePlayHistory()
        if (playHistory.current != 0L && playHistory.current < player.endProgress) {
            player.seekTo(playHistory.current)
        }

        if (playHistory.current == 0L) {
            // 跳过片头
            val skipStart = SPConfig.PLAYER_SKIP_START.get<Boolean>(false)!!
            if (skipStart) {
                val skipTime = SPConfig.PLAYER_SKIP_START_TIME.get<Int>(0)!!
                if (skipTime != 0) {
                    player.seekTo(skipTime.toLong())
                    binding.dyPlayer.showToast("已自动跳过片头，正在播放：${binding.dyPlayer.title}")
                }
            }
        }

        val autoFullscreen: Boolean = SPConfig.PLAYER_AUTO_FULLSCREEN.get<Boolean>(false)!!
        if (autoFullscreen) {
            binding.dyPlayer.isFullScreen = true
        }
    }

    override fun onProgressChanged(currentProgress: Long) {
        var duration = playHistory.duration

        if (duration == 0L) {
            duration = binding.dyPlayer.endProgress
            playHistory.duration = binding.dyPlayer.endProgress
        }

        if (!binding.dyPlayer.isSeeking) {
            val skipEnd = SPConfig.PLAYER_SKIP_END.get<Boolean>(false)!!
            if (skipEnd && binding.routeView.hasNextSelection()) {
                val skipTime = SPConfig.PLAYER_SKIP_END_TIME.get<Int>(0)!!.toLong()
                if (skipTime != 0L && duration - binding.dyPlayer.realCurrentProgress <= skipTime) {
                    binding.routeView.nextSelection()
                    // 跳过片尾
                    binding.dyPlayer.showToast("已自动跳过片尾，正在播放：${binding.dyPlayer.title}")
                    return
                }
            }

            playHistory.current = currentProgress
            playHistory.progress = (currentProgress * 100 / duration).toInt()
            updatePlayHistory()
        }
    }

    override fun onPlayStateChanged(newState: Int) {
        when (newState) {
            BasePlayer.STATE_COMPLETED -> {
                val autoNext = SPConfig.PLAYER_AUTO_NEXT.get(true)!!
                // 判断是否是最后一集
                if (((playHistory.selection) ?: 1) >= playHistory.totalSelection) {
                    playHistory.progress = 100
                    playHistory.current = playHistory.duration
                } else if (!autoNext) {
                    // 如果不是最后一集，就将进度置0
                    playHistory.progress = 0
                    playHistory.current = 0
                }

                updatePlayHistory()

                if (!binding.routeView.hasNextSelection()) {
                    binding.dyPlayer.showToast("已全部播放完成，感谢观看！")
                }

                // 完成了，播放下一集
                if (autoNext) {
                    binding.routeView.nextSelection()
                } else {
                    // 暂停状态
                    binding.dyPlayer.pause()
                }
            }
        }
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