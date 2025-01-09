package com.dongyu.movies

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.dongyu.movies.utils.Checker
import com.dongyu.movies.utils.ContextUtils
import com.dongyu.movies.utils.EncryptUtils
import com.dongyu.movies.utils.SSLIgnore
import com.drake.statelayout.StateConfig
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.wanban.screencast.ScreenCastUtils
import org.litepal.LitePal

/**
 * App入口
 */
class MoviesApplication : Application() {

    companion object {
        /**
         * 全局AppContext
         */
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        /**
         * 暴露给本地代码的方法，用于获取对象的简单类名
         */
        @JvmStatic
        fun getApplicationName(obj: Any): String? {
            return obj.javaClass.getSimpleName()
        }
    }

    init {
        // 这里对App环境进行检测，如果App签名不同，则不允许成功运行
        val ctx = ContextUtils.getContext()
        Checker.verifySignature(ctx)
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        // 初始化异常处理器
        CrashHandler.getInstance().init()
        // MD3动态配色
        // DynamicColors.applyToActivitiesIfAvailable(this)
        // 初始化数据库操作程序
        LitePal.initialize(this)
        // 初始化加密程序
        EncryptUtils.getInstance().init()
        // 初始化投屏
        ScreenCastUtils.init(this)
        // 忽略ssl错误
        SSLIgnore.init()
        // 初始化广告
        /*val admConfig = ADMConfig.Builder()
            .appKey(ADConfig.APP_ID)
            // .oaid(ADConfig.OA_ID)
            .debug(false)
            .build()
        SadManager.getInstance().initAd(this, admConfig)*/

        // 一些配置信息
        StateConfig.apply {
            loadingLayout = R.layout.layout_loading
            emptyLayout = R.layout.layout_empty
            errorLayout = R.layout.layout_error
            setRetryIds(R.id.tv_iv, R.id.tv_msg)
            onError {
                // 全局错误处理
            }
        }
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            ClassicsHeader(context)
            // MaterialHeader(context)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            ClassicsFooter(context)
        }
    }

    /**
     * 检查整个Application有没有被代理
     */
    external fun checkApplicationNative(): Boolean
}