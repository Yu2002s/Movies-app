package com.dongyu.movies.download

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
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

        private const val THREAD_COUNT = 10
    }

    private val downloadBinder = DownloadBinder()

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
        executorService.shutdown()
    }

    /**
     * 下载队列
     */
    private val _downloadList = mutableMapOf<Download, M3U8Downloader>()

    var downloadListener: DownloadListener? = null

    /**
     * 线程池
     */
    private val executorService = ThreadPoolExecutor(
        THREAD_COUNT, THREAD_COUNT,
        0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue()
    )

    init {

    }

    fun isContainsDownload(download: Download): Boolean {
        return _downloadList.containsKey(download)
    }

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

    fun resumeOrStopDownload(download: Download) {
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

    fun removeDownload(download: Download) {
        _downloadList[download]?.stop()
        val file = File(download.downloadPath)
        file.delete()
        val parentFile = file.parentFile
        parentFile?.let {
            if (it.list().isNullOrEmpty()) {
                it.delete()
            }
        }
        executorService.execute {
            download.delete()
        }
        _downloadList.remove(download)
        download.listener = null
    }

    private fun startDownload(download: Download) {
        download.listener = object : DownloadListener {
            override fun onDownload(download: Download) {
                if (download.status == DownloadStatus.COMPLETED) {
                    _downloadList.remove(download)
                    download.listener = null
                }
                Log.d(TAG, "downloadListener: ${download}")
                downloadListener?.onDownload(download)
            }
        }
        val m3U8Downloader = M3U8Downloader(executorService, download)
        _downloadList[download] = m3U8Downloader
        m3U8Downloader.download()
    }

    fun addDownload(url: String, name: String = "", groupName: String = "") {
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