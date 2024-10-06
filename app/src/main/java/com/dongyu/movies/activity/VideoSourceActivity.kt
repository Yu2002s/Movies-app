package com.dongyu.movies.activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.R
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.databinding.ActivityVideoSourceBinding
import com.dongyu.movies.download.DownloadService
import com.dongyu.movies.fragment.VideoSourceFragment
import com.dongyu.movies.model.movie.VideoSource
import com.dongyu.movies.utils.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import java.util.ArrayList

class VideoSourceActivity : BaseActivity(), ServiceConnection, MenuProvider {

    private val binding by lazy {
        ActivityVideoSourceBinding.inflate(layoutInflater)
    }

    private var _downloadService: DownloadService? = null
    private val downloadService get() = _downloadService!!

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    companion object {

        private const val PARAM_POSITION = "position"
        private const val PARAM_SOURCE = "sources"
        private const val PARAM_NAME = "name"

        @JvmStatic
        fun start(sources: List<VideoSource>, position: Int, name:String) {
            val starter = Intent(MoviesApplication.context, VideoSourceActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(PARAM_POSITION, position)
                .putExtra(PARAM_NAME, name)
                .putParcelableArrayListExtra(PARAM_SOURCE, sources as ArrayList<out Parcelable>)
            MoviesApplication.context.startActivity(starter)
        }
    }

    private val sources = mutableListOf<VideoSource>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindService(Intent(this, DownloadService::class.java), this, Context.BIND_AUTO_CREATE)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addMenuProvider(this)

        checkPermission()

        sources.clear()
        sources.addAll(intent.getParcelableArrayListExtra(PARAM_SOURCE)!!)
        val current = intent.getIntExtra(PARAM_POSITION, 0)

        binding.vp.adapter = SourceAdapter()
        binding.vp.currentItem = current

        TabLayoutMediator(binding.tab, binding.vp) { tab, position ->
            tab.text = sources[position].name
        }.attach()

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) {
                    "权限被拒绝，无法下载文件".showToast()
                }
            }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            if (!Environment.isExternalStorageManager()) {
                "请开启访问所有文件权限，以便下载文件".showToast()
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, "package:${packageName}".toUri())
                startActivity(intent)
            }
        }
    }

    fun addDownload(url: String, name: String, groupName: String) {
        // 下载文件
        downloadService.addDownload(url, name, groupName)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        _downloadService = (service as? DownloadService.DownloadBinder)?.getService()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _downloadService = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add("下载管理").apply {
            setIcon(R.drawable.baseline_arrow_circle_down_24)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        com.dongyu.movies.utils.startActivity<DownloadActivity>()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _downloadService = null
        unbindService(this)
        removeMenuProvider(this)
    }

    private inner class SourceAdapter : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        override fun getItemCount() = sources.size

        override fun createFragment(position: Int) =
            VideoSourceFragment.newInstance(sources[position].items)

    }
}