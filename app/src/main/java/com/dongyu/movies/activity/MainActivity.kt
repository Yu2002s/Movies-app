package com.dongyu.movies.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isInvisible
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.R
import com.dongyu.movies.adapter.SearchSuggestAdapter
import com.dongyu.movies.config.AppConfig
import com.dongyu.movies.data.search.SearchUiResult
import com.dongyu.movies.data.search.SearchUiSuggest
import com.dongyu.movies.data.update.Update
import com.dongyu.movies.databinding.ActivityMainBinding
import com.dongyu.movies.dialog.RouteDialog
import com.dongyu.movies.event.OnItemClickListener
import com.dongyu.movies.fragment.HistoryFragment
import com.dongyu.movies.fragment.HomeFragment
import com.dongyu.movies.fragment.home.MainFragment
import com.dongyu.movies.fragment.search.SearchFragment
import com.dongyu.movies.fragment.setting.SettingFragment
import com.dongyu.movies.viewmodel.MainActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<MainActivityViewModel>()

    private val fragments = mutableListOf<Class<*>>()

    private val searchSuggestAdapter by lazy {
        SearchSuggestAdapter()
    }

    private val inputMethodManager by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)

        collectData()
        initFragments()
        initViews()
        initEvents()
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewModel.updateState.collect { result ->
                if (result == null)
                    return@collect
                checkUpdate(result)
            }
        }

        lifecycleScope.launch {
            viewModel.searchSuggestState.collectLatest {
                searchSuggestAdapter.submitList(it)
            }
        }

        val searchResult = binding.searchResult
        lifecycleScope.launch {
            viewModel.searchUiState.collect {
                binding.searchSuggest.isInvisible = it !is SearchUiSuggest
                searchResult.isInvisible = it !is SearchUiResult
                binding.mainSearchBar.setText(it.name)

                if (it is SearchUiResult) {
                    searchResult.getFragment<SearchFragment>().search(it.name)
                }
            }
        }
    }

    private fun initFragments() {
        fragments.add(HomeFragment::class.java)
        fragments.add(HistoryFragment::class.java)
        fragments.add(SettingFragment::class.java)
    }

    private fun initViews() {
        val mainViewPager = binding.mainViewpager
        val bottomNav = binding.bottomNav
        val viewPager = binding.mainViewpager
        viewPager.isUserInputEnabled = false
        viewPager.apply {
            offscreenPageLimit = 3
            adapter = MainViewPagerAdapter(supportFragmentManager, lifecycle, fragments)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val menuItem = bottomNav.menu.getItem(position)
                    if (!menuItem.isChecked) {
                        menuItem.isChecked = true
                    }
                }
            })
        }

        bottomNav.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
        bottomNav.setOnItemSelectedListener {
            val position = when (it.itemId) {
                R.id.nav_home -> 0
                R.id.nav_history -> 1
                /*R.id.nav_download -> 2*/
                R.id.nav_setting -> 2
                else -> 0// throw IllegalStateException()
            }
            mainViewPager.setCurrentItem(position, false)
            true
        }
        /*bottomNav.post {
            mainViewPager.updatePadding(bottom = bottomNav.height)
        }*/

        val searchSuggest = binding.searchSuggest
        searchSuggest.adapter = searchSuggestAdapter
    }

    private fun initEvents() {
        val searchEditText = binding.mainSearchView.editText
        searchEditText.addTextChangedListener {
            if (!searchEditText.hasFocus()) {
                return@addTextChangedListener
            }
            val text = it.toString().trim()
            viewModel.changeSearchState(SearchUiSuggest(text))
        }
        searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(
                    v.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                val text = v.text.toString().trim()
                if (text.isBlank()) {
                    return@setOnEditorActionListener false
                }
                viewModel.changeSearchState(SearchUiResult(text))
            }
            false
        }

        searchEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // 显示搜索
                viewModel.changeSearchState(
                    SearchUiSuggest(
                        (v as EditText).text.toString().trim()
                    )
                )
            }
        }

        searchEditText.setOnKeyListener { v, keyCode, event ->
            if (event.keyCode == KeyEvent.KEYCODE_SEARCH || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                v.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(
                    v.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                val text = (v as EditText).text.toString().trim()
                if (text.isBlank()) {
                    return@setOnKeyListener false
                }
                viewModel.changeSearchState(SearchUiResult(text))
            }
            false
        }

        searchSuggestAdapter.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val name = searchSuggestAdapter.currentList[position].name
                binding.mainSearchBar.setText(name)
                binding.mainSearchView.setText(name)
                viewModel.changeSearchState(SearchUiResult(name))
            }
        }

        // 监听返回键
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.mainSearchView.isShowing) {
                    // 如果显示了搜索内容，则先隐藏
                    binding.mainSearchView.hide()
                } else {
                    finish()
                }
            }
        })

        binding.mainSearchBar.setNavigationOnClickListener {
            binding.mainSearchView.show()
        }

        binding.mainSearchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.rank -> {
                    for (fragment in supportFragmentManager.fragments) {
                        if (fragment is HomeFragment) {
                            for (child in fragment.childFragmentManager.fragments) {
                                if (child is MainFragment) {
                                    child.showRankDialog()
                                    break
                                }
                            }
                        }
                    }
                }
                R.id.route -> {
                    RouteDialog(this)
                }
            }
            true
        }

        val statusBarColor = getStatusBarColor()
        var isCollapsed = false
        binding.mainAppBar.addOnOffsetChangedListener { appBarLayout, offset ->
            if (binding.mainSearchView.isShowing) return@addOnOffsetChangedListener
            if (offset == -appBarLayout.totalScrollRange) {
                isCollapsed = true
                // 折叠状态
                window.statusBarColor = statusBarColor
            } else if (isCollapsed) {
                // 展开状态
                isCollapsed = false
                window.statusBarColor = Color.TRANSPARENT
            }
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

    private fun getStatusBarColor(): Int {
        val typeValue = TypedValue()
        theme.resolveAttribute(android.R.attr.windowBackground, typeValue, true)
        return ContextCompat.getColor(this, typeValue.resourceId)
    }

    fun showAppBar() {
        binding.mainAppBar.setExpanded(true)
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