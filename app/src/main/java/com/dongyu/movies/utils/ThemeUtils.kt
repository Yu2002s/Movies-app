package com.dongyu.movies.utils

import android.app.Activity
import com.dongyu.movies.R
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.event.ThemeObserver
import com.dongyu.movies.utils.SpUtils.getRequired
import com.dongyu.movies.utils.SpUtils.put

object ThemeUtils {

    /**
     * 跟随系统
     */
    const val THEME_AUTO = "auto"

    /**
     * 亮色
     */
    const val THEME_LIGHT = "light"

    /**
     * 深色主题
     */
    const val THEME_DARK = "dark"

    private val themeObservers = mutableListOf<ThemeObserver>()

    var currentTheme = THEME_AUTO
        get() = SPConfig.APP_THEME.getRequired<String>(THEME_DARK)
        set(value) {
            if (field != value) {
                SPConfig.APP_THEME.put(value)
                field = value
            }
        }

    fun setTheme(activity: Activity) {
        activity.setTheme(when (currentTheme) {
            THEME_AUTO -> R.style.Theme_Movies
            THEME_LIGHT -> R.style.Theme_Movies_Light
            THEME_DARK -> R.style.Theme_Movies_Dark
            else -> R.style.Theme_Movies_Dark
        })
    }

    fun setTheme(activity: Activity, theme: String) {
        currentTheme = theme
        setTheme(activity)
    }

    fun addObserver(observer: ThemeObserver) {
        themeObservers.add(observer)
    }

    fun removeObserver(observer: ThemeObserver) {
        themeObservers.remove(observer)
    }

    fun notifyThemeChanged() {
        val theme = currentTheme
        themeObservers.forEach { it.onThemeChanged(theme) }
    }
}