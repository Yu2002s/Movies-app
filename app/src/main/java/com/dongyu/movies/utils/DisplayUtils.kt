package com.dongyu.movies.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.view.WindowManager
import com.bytedance.pangle.transform.ZeusTransformUtils.getResources
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.utils.SpUtils.get

/**
 * dp转像素
 */
fun Int.dp2px(context: Context = MoviesApplication.context): Int {
    return (context.resources.displayMetrics.density * this + 0.5).toInt()
}

fun Activity.getWindowWidth(): Int {
   return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.windowManager.currentWindowMetrics.bounds.width()
    } else {
        this.windowManager.defaultDisplay.width
    }
}

fun Activity.getWindowHeight(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.windowManager.currentWindowMetrics.bounds.height()
    } else {
        this.windowManager.defaultDisplay.height
    }
}

fun getWindowWidth(): Int {
    val context = MoviesApplication.context
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowManager.currentWindowMetrics.bounds.width()
    } else {
        windowManager.defaultDisplay.width
    }
}

fun getWindowHeight(): Int {
    val context = MoviesApplication.context
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowManager.currentWindowMetrics.bounds.height()
    } else {
        windowManager.defaultDisplay.height
    }
}

/**
 * 是否为深色模式
 */
val isDarkMode: Boolean get() {
    val context = MoviesApplication.context
    val theme = ThemeUtils.currentTheme
    if (theme != ThemeUtils.THEME_AUTO) {
        return theme == ThemeUtils.THEME_DARK
    }
    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
}
