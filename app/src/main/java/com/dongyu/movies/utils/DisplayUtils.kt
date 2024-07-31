package com.dongyu.movies.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.core.view.DisplayCompat
import androidx.core.view.WindowCompat
import com.dongyu.movies.MoviesApplication

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