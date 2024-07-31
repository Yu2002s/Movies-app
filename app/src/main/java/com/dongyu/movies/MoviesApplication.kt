package com.dongyu.movies

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.core.content.ContextCompat
import com.dongyu.movies.utils.Checker
import com.dongyu.movies.utils.ContextUtils
import com.dongyu.movies.utils.EncryptUtils
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.utilities.DynamicScheme
import org.litepal.LitePal


class MoviesApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        const val APP_PAGE_HOME = "http://jdynb.xyz/movie"

        const val GROUP_URL = "https://t.me/dongyumovies"

        const val GIT_RELEASE_URL = "https://gitee.com/jdy2002/movies/releases"

        const val QQ_GROUP_URL = "http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=ftRJyAMQkZG_cVmVsMjFiMIhclwHIBsz&authKey=t26K2XvCPFEmaWGtzekfP8f5n86ulJtW%2F23xOqyKw%2Fk8RNVYDKFAnbSZyjvYkWyj&noverify=0&group_code=697470084"

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

    override fun onCreate() {
        super.onCreate()
        context = this
        CrashHandler.getInstance().init()
        DynamicColors.applyToActivitiesIfAvailable(this)
        LitePal.initialize(this)
        EncryptUtils.getInstance().init()
    }

    // JNI接口方法
    external fun checkApplicationNative(): Boolean
}