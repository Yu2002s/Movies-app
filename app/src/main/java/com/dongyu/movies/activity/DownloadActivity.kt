package com.dongyu.movies.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.R
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.databinding.ActivityDownloadBinding
import com.dongyu.movies.databinding.ItemListDownloadBinding
import com.dongyu.movies.download.DownloadListener
import com.dongyu.movies.download.DownloadService
import com.dongyu.movies.model.download.Download
import com.dongyu.movies.model.download.DownloadStatus
import com.dongyu.movies.model.movie.VideoType
import com.dongyu.movies.utils.formatBytes
import com.dongyu.movies.utils.showToast
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import org.litepal.extension.find

class DownloadActivity : BaseActivity(), ServiceConnection, DownloadListener {

    companion object {

        private const val TAG = "DownloadActivity"

    }

    private val binding by lazy {
        ActivityDownloadBinding.inflate(layoutInflater)
    }

    private var _downloadService: DownloadService? = null
    private val downloadService get() = _downloadService!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindService(Intent(this, DownloadService::class.java), this, Context.BIND_AUTO_CREATE)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rv.setup {
            addType<Download>(R.layout.item_list_download)
            onCreate {
                getBinding<ItemListDownloadBinding>().sw.setOnClickListener {
                    downloadService.resumeOrPauseDownload(getModel())
                }
            }
            onBind {
                val model = getModel<Download>()
                getBinding<ItemListDownloadBinding>().apply {
                    name.text = model.name
                    status.text =
                        model.statusStr + " " + formatBytes(model.currentByte) + " " + model.progress + "%"
                    sw.setImageResource(
                        if (model.status == DownloadStatus.DOWNLOADING)
                            R.drawable.baseline_pause_circle_outline_24
                        else R.drawable.baseline_arrow_circle_down_24
                    )
                    sw.isVisible = model.status != DownloadStatus.COMPLETED
                    if (model.progress == 0 && model.isDownloading) {
                        progressBar.isIndeterminate = true
                    } else {
                        progressBar.isIndeterminate = false
                        progressBar.setProgressCompat(model.progress, true)
                    }
                    progressBar.isVisible = model.status != DownloadStatus.COMPLETED
                }
            }

            R.id.item.onClick {
                val model = getModel<Download>()
                if (model.status != DownloadStatus.COMPLETED) {
                    downloadService.resumeOrPauseDownload(model)
                    return@onClick
                }
                // 打开播放
                VideoActivity.play(VideoType.NORMAL, model.downloadPath, model.name)
            }

            R.id.item.onLongClick {
                val model = getModel<Download>()
                downloadService.removeDownload(model)
                mutable.removeAt(modelPosition)
                notifyItemRemoved(modelPosition)
            }
        }

        binding.rvl.onRefresh {
            lifecycleScope.launch {
                val offset = (index - 1) * 10
                val downloads = withContext(Dispatchers.IO) {
                    LitePal.offset(offset).limit(10).order("updateAt desc").find<Download>()
                        .onEach { download ->
                            download.status = downloadService.getDownloadStatus(download)
                        }
                }
                addData(downloads)
            }
        }

        // android 11以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                "请授权访问存储空间以下载文件".showToast()
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        "package:$packageName".toUri()
                    )
                )
            }
        }
    }

    override fun onDownload(download: Download) {
        if (binding.rv.models == null) {
            return
        }
        val model = binding.rv.models!!.indexOf(download)
        runOnUiThread {
            if (model == -1) {
                // binding.rv.mutable.add(0, download)
                // binding.rv.adapter?.notifyItemInserted(0)
            } else {
                binding.rv.mutable[model] = download
                binding.rv.adapter?.notifyItemChanged(model)
            }
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        _downloadService = (service as DownloadService.DownloadBinder).getService()
        _downloadService?.let {
            it.downloadListener = this
            binding.rvl.showLoading()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _downloadService = null
    }

    override fun onInsetChanged(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
        return true
    }

    override fun getRecyclerView(): RecyclerView {
        return binding.rv
    }

    override fun onDestroy() {
        super.onDestroy()
        _downloadService?.downloadListener = null
        unbindService(this)
        _downloadService = null
    }
}