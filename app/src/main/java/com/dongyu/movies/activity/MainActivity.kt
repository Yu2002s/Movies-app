package com.dongyu.movies.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dongyu.movies.R
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.config.AppConfig
import com.dongyu.movies.model.update.Update
import com.dongyu.movies.databinding.ActivityMainBinding
import com.dongyu.movies.download.DownloadService
import com.dongyu.movies.fragment.HistoryCollectFragment
import com.dongyu.movies.fragment.LiveFragment
import com.dongyu.movies.fragment.MineFragment
import com.dongyu.movies.fragment.home.HomeFragment
import com.dongyu.movies.utils.isDarkMode
import com.dongyu.movies.viewmodel.MainActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, DownloadService::class.java))
        setContentView(binding.root)

        collectData()
        initFragments()
        initViews()
        checkPermission()
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
                setTitle("发现新本${update.versionName}(${update.versionCode})")
                setMessage(update.content)
                setCancelable(false)
                setPositiveButton("下载更新") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.url ?: update.altUrl))
                    startActivity(intent)
                    exitProcess(0)
                }
                setNegativeButton("Telegram") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.GROUP_URL))
                    startActivity(intent)
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

    private fun checkPermission() {
        // 检查发送通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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