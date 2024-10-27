package com.dongyu.movies.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.R
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.config.AppConfig
import com.dongyu.movies.databinding.ActivityMainBinding
import com.dongyu.movies.download.DownloadService
import com.dongyu.movies.fragment.LiveFragment
import com.dongyu.movies.fragment.MineFragment
import com.dongyu.movies.fragment.home.HomeFragment
import com.dongyu.movies.model.update.Update
import com.dongyu.movies.network.AppRepository
import com.dongyu.movies.parser.BaseParser
import com.dongyu.movies.parser.impl.DDParser
import com.dongyu.movies.utils.isDarkMode
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.viewmodel.MainActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<MainActivityViewModel>()

    private val fragments = mutableListOf<Class<*>>()

    @SuppressLint("JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, DownloadService::class.java))
        setContentView(binding.root)

        collectData()
        initFragments()
        initViews()
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkUpdate()
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewModel.updateState.collect { result ->
                if (result == null)
                    return@collect
                checkUpdate(result)
            }
        }
    }

    private fun initFragments() {
        fragments.add(HomeFragment::class.java)
        // fragments.add(HistoryCollectFragment::class.java)
        fragments.add(LiveFragment::class.java)
        fragments.add(MineFragment::class.java)
    }

    private fun initViews() {
        val mainViewPager = binding.mainViewpager
        val bottomNav = binding.bottomNav
        val viewPager = binding.mainViewpager
        val insetsController = WindowCompat.getInsetsController(window, binding.root)
        viewPager.apply {
            isUserInputEnabled = false
            // offscreenPageLimit = 2
            adapter = MainViewPagerAdapter(supportFragmentManager, lifecycle, fragments)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val menuItem = bottomNav.menu.getItem(position)
                    if (!menuItem.isChecked) {
                        menuItem.isChecked = true
                    }
                    insetsController.isAppearanceLightStatusBars =
                        !isDarkMode && menuItem.title != getString(R.string.nav_mine)
                }
            })
        }

        bottomNav.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
        bottomNav.setOnItemSelectedListener {
            val position = when (it.itemId) {
                R.id.nav_home -> 0
                R.id.nav_live -> 1
                /*R.id.nav_history -> 1*/
                /*R.id.nav_download -> 2*/
                R.id.nav_mine -> 2
                else -> 0// throw IllegalStateException()
            }
            mainViewPager.setCurrentItem(position, false)
            true
        }
    }

    private fun checkUpdate(result: Result<Update?>) {
        result.onSuccess { update ->
            if (update == null) {
                return@onSuccess
            }
            MaterialAlertDialogBuilder(this).apply {
                setTitle("发现新版本${update.versionName}(${update.versionCode})")
                setMessage(update.content)
                setCancelable(false)
                setPositiveButton("下载更新") { _, _ ->
                    getUpdateUrl(update)
                }
                setNegativeButton("Telegram") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.GROUP_URL))
                    startActivity(intent)
                    exitProcess(0)
                }
                setNeutralButton("外部下载") { _, _ ->
                    startActivity(Intent(Intent.ACTION_VIEW, update.url?.toUri()))
                    exitProcess(0)
                }
                show()
            }
        }.onFailure {
            MaterialAlertDialogBuilder(this).apply {
                setTitle("检查更新失败")
                setMessage("服务器可能维护升级中...请耐心等待！请检查网络后尝试重新打开或使用备用地址更新")
                setCancelable(false)
                setPositiveButton("备用地址") { _, _ ->
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.GIT_RELEASE_URL))
                    startActivity(intent)
                    exitProcess(0)
                }
                setNeutralButton("Telegram") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.GROUP_URL))
                    startActivity(intent)
                    exitProcess(0)
                }
                setNegativeButton("重新打开") { _, _ ->
                    finish()
                    startActivity(intent)
                }
                show()
            }
        }
    }

    private fun getUpdateUrl(update: Update) {
        val url = update.url ?: update.altUrl
        val key = url.substring(url.lastIndexOf('/') + 1)

        fun externalDownload() {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            "正在访问下载网页，请点击底部下载文件按钮".showToast()
            exitProcess(0)
        }

        lifecycleScope.launch {
            AppRepository.getUpdateUrl(key).onSuccess {
                if (it.directLink.isEmpty()) {
                    return@onSuccess externalDownload()
                }
                val request = DownloadManager.Request(it.directLink.toUri())
                    .setTitle(getString(R.string.app_name) + update.versionName + ".apk")
                    .setDescription("冬雨影视更新")
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedOverRoaming(true)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "冬雨影视${update.versionName}.apk")
                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)
                "正在下载更新，请留意通知栏下载进度".showToast()
            }.onFailure {
                Log.e(TAG, "getUpdateUrl: $it")
                externalDownload()
            }
        }
    }

    private fun checkPermission() {
        // 检查发送通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
            }
        }
    }

    private class MainViewPagerAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle,
        private val fragments: List<Class<*>>
    ) :
        FragmentStateAdapter(fm, lifecycle) {
        override fun getItemCount() = fragments.size

        override fun createFragment(position: Int) = fragments[position]
            .getConstructor().newInstance() as Fragment

    }
}