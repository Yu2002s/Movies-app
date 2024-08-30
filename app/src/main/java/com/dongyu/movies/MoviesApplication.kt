package com.dongyu.movies

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.cat.sdk.ADMConfig
import com.cat.sdk.SadManager
import com.dongyu.movies.config.ADConfig
import com.dongyu.movies.utils.Checker
import com.dongyu.movies.utils.ContextUtils
import com.dongyu.movies.utils.EncryptUtils
import com.google.android.material.color.DynamicColors
import com.wanban.screencast.ScreenCastUtils
import org.litepal.LitePal

class MoviesApplication : Application() {

    companion object {
        /**
         * 全局AppContext
         */
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        @JvmStatic
        // 暴露给本地代码的方法，用于获取对象的简单类名
        fun getApplicationName(obj: Any): String? {
            return obj.javaClass.getSimpleName()
        }
    }

    init {
        val ctx = ContextUtils.getContext()
        Checker.verifySignature(ctx)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        // 初始化异常处理器
        CrashHandler.getInstance().init()
        // MD3动态配色
        DynamicColors.applyToActivitiesIfAvailable(this)
        // 初始化数据库操作程序
        LitePal.initialize(this)
        // 初始化加密程序
        EncryptUtils.getInstance().init()
        // 初始化投屏
        ScreenCastUtils.init(this)
        // 初始化广告
        val admConfig = ADMConfig.Builder()
            .appKey(ADConfig.APP_ID)
            .oaid(ADConfig.OA_ID)
            .debug(true)
            .build()
        SadManager.getInstance().initAd(this, admConfig)
    }

    // JNI接口方法
    external fun checkApplicationNative(): Boolean
}