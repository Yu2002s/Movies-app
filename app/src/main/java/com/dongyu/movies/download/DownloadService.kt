package com.dongyu.movies.download

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.R
import com.dongyu.movies.activity.DownloadActivity
import com.dongyu.movies.model.download.Download
import com.dongyu.movies.model.download.DownloadStatus
import com.dongyu.movies.utils.showToast
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class DownloadService : Service() {

    companion object {
        private val TAG = DownloadService::class.simpleName
        const val DOWNLOAD_CHANNEL_ID = "download_service"
    }

    private val downloadBinder = DownloadBinder()

    private lateinit var notificationManager: NotificationManagerCompat

    inner class DownloadBinder : Binder() {
        fun getService() = this@DownloadService
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind $this")
        return downloadBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind $this")
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate $this")

        // 构建通知渠道
        notificationManager = NotificationManagerCompat.from(this)

        val notificationChannel = NotificationChannelCompat.Builder(
            DOWNLOAD_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setName(this::class.simpleName)
            .setVibrationEnabled(false)
            .setShowBadge(true)
            .setDescription("App内部的下载服务展示状态通知")
            .build()

        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand $this")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy $this")
        _downloadList.clear()
        downloadListener = null
        executorThreadPool.shutdownNow()
        notificationManager.cancelAll()
    }

    /**
     * 下载队列
     */
    private val _downloadList = mutableMapOf<Download, M3U8Downloader>()

    var downloadListener: DownloadListener? = null

    /**
     * 其他业务线程池
     */
    private val executorThreadPool = ThreadPoolExecutor(
        5, 5, 0, TimeUnit.SECONDS, LinkedBlockingQueue()
    )

    /**
     * 下载队列中是否存在下载
     */
    fun isContainsDownload(download: Download): Boolean {
        return _downloadList.containsKey(download)
    }

    /**
     * 获取真实的下载状态
     */
    fun getDownloadStatus(download: Download): Int {
        val downloader = _downloadList[download]
        if (download.status == DownloadStatus.DOWNLOADING) {
            if (downloader == null) {
                return DownloadStatus.UNSTART
            }
        } else if (download.status == DownloadStatus.UNSTART) {
            if (downloader != null) {
                return DownloadStatus.DOWNLOADING
            }
        }
        return download.status
    }

    /**
     * 恢复或者暂停下载
     */
    fun resumeOrPauseDownload(download: Download) {
        val m3U8Downloader = _downloadList[download]
        if (m3U8Downloader == null) {
            startDownload(download)
            return
        }
        if (download.status == DownloadStatus.DOWNLOADING) {
            // 暂停
            m3U8Downloader.pause()
        } else {
            m3U8Downloader.resume()
        }
    }

    /**
     * 删除下载
     */
    fun removeDownload(download: Download) {
        // 子线程中操作数据库
        executorThreadPool.execute {
            download.delete()
        }
        _downloadList[download]?.stop()
        val file = File(download.downloadPath)
        file.delete()
        val parentFile = file.parentFile
        parentFile?.let {
            if (it.list().isNullOrEmpty()) {
                it.delete()
            }
        }

        _downloadList.remove(download)
        download.listener = null
        notificationManager.cancel(download.id.toInt())
    }

    private fun startDownload(download: Download) {
        download.listener = object : DownloadListener {
            override fun onDownload(download: Download) {
                updateDownloadStatus(download)
                // Log.d(TAG, "downloadListener: ${download}")
                downloadListener?.onDownload(download)
            }
        }
        val m3U8Downloader = M3U8Downloader(executorThreadPool, download)
        _downloadList[download] = m3U8Downloader
        m3U8Downloader.download()
    }

    private val notification by lazy {
        NotificationCompat.Builder(this, DOWNLOAD_CHANNEL_ID)
            .setWhen(System.currentTimeMillis())
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, DownloadActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
    }

    @SuppressLint("MissingPermission")
    private fun updateDownloadStatus(download: Download) {
        notification.setContentText(download.statusStr)
        notification.setWhen(System.currentTimeMillis())
        when (download.status) {
            DownloadStatus.UNSTART -> {

            }

            DownloadStatus.DOWNLOADING -> {
                notification.setSmallIcon(R.drawable.baseline_arrow_circle_down_24)
                notification.setProgress(100, download.progress, false)
            }

            DownloadStatus.PREPARE -> {
                notification.setOngoing(true)
                notification.setContentTitle(download.name)
                notification.setProgress(100, 0, true)
            }

            DownloadStatus.ERROR -> {
                notification.setOngoing(false)
                notification.setSmallIcon(R.drawable.baseline_close_24)
            }

            DownloadStatus.MERGE -> {

            }

            DownloadStatus.PAUSE -> {
                notification.setSmallIcon(R.drawable.baseline_pause_circle_outline_24)
            }

            DownloadStatus.COMPLETED -> {
                notification.setOngoing(false)
                _downloadList.remove(download)
                download.listener = null
                notification.setProgress(0, 0, false)
                notification.setSmallIcon(R.drawable.baseline_done_24)
            }
        }
        notificationManager.notify(download.id.toInt(), notification.build())
    }

    fun addDownload(url: String, name: String = "", groupName: String = "") {
        if (_downloadList.size > 4) {
            "为了更好的体验，同时只能下载4个任务(包括已暂停任务)".showToast()
            return
        }

        // 开始下载
        val download = Download(url = url, name = name, groupName = groupName)

        if (isContainsDownload(download)) {
            Log.w(TAG, "下载队列中已存在: $url")
            "$name 下载队列中已存在".showToast()
            return
        }

        "$name 已加入下载队列".showToast()
        startDownload(download)
    }
}