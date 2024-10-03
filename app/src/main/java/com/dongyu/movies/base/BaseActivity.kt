package com.dongyu.movies.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.event.ThemeObserver
import com.dongyu.movies.utils.ThemeUtils

open class BaseActivity: AppCompatActivity(), ThemeObserver {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTheme()
        initWindow()
    }

    private fun initTheme() {
        ThemeUtils.setTheme(this)
        ThemeUtils.addObserver(this)
    }

    fun setAppTheme(theme: String) {
        ThemeUtils.setTheme(this, theme)
    }

    private fun initWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            ViewCompat.setOnApplyWindowInsetsListener(v, null)
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            if (onInsetChanged(statusBarHeight, navigationBarHeight)) {
                val rootView: ViewGroup = v.findViewById<ViewGroup>(android.R.id.content)
                    .getChildAt(0) as ViewGroup
                (getRecyclerView() ?: getRecyclerView(rootView))?.apply {
                    clipToPadding = false
                    updatePadding(bottom = navigationBarHeight)
                }
            }
            insets
        }
    }

    override fun onThemeChanged(theme: String) {
        ThemeUtils.setTheme(this)
        recreate()
    }

    /**
     * 对返回事件进行统一处理
     */
    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * @return 返回true将自动对recyclerview进行底栏处理
     */
    protected open fun onInsetChanged(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
        return false
    }

    /**
     * 返回需要处理底栏的RecyclerView
     */
    protected open fun getRecyclerView(): RecyclerView? {
        return null
    }

    /**
     * 获取当前页面的第一个RecyclerView
     */
    private fun getRecyclerView(view: ViewGroup): RecyclerView? {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            if (child is RecyclerView) {
                return child
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeUtils.removeObserver(this)
    }

}