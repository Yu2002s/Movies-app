package com.dongyu.movies

import android.content.Intent
import android.os.Build
import android.util.Log
import com.dongyu.movies.activity.CrashActivity
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

/**
 * App崩溃处理
 */
class CrashHandler: Thread.UncaughtExceptionHandler {

    private lateinit var defaultHandler: Thread.UncaughtExceptionHandler

    companion object {

        private var instance: CrashHandler? = null

        fun getInstance(): CrashHandler {
            if (instance == null) {
                instance = CrashHandler()
            }
            return instance!!
        }

    }

    fun init() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()!!
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!handlerException(e)) {
            defaultHandler.uncaughtException(t, e)
        } else {
            exitProcess(0)
        }
    }

    private fun handlerException(e: Throwable?): Boolean {
        if (e == null) {
            return false
        }
        try {
            val exception = getException(e)

            Log.e("jdy", exception)
            val context = MoviesApplication.context
            context.startActivity(Intent(context, CrashActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("log", exception)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun getException(e: Throwable): String {
        val stringBuilder = StringBuilder()
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        e.printStackTrace(printWriter)
        var cause = e.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.flush()
        printWriter.close()
        val context = MoviesApplication.context
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        stringBuilder.append("提示：请先尝试清除软件数据，如复现，请反馈\n")
        // stringBuilder.append("联系QQ：2475058223\n")
        stringBuilder.append("手机型号: " + Build.MODEL + "-" + Build.BRAND)
        stringBuilder.append("\n当前软件版本：${packageInfo.versionName}\n")
        stringBuilder.append("SDK版本: " + Build.VERSION.SDK_INT)
        stringBuilder.append("\n手机系统：" + Build.VERSION.INCREMENTAL)

        stringBuilder.append("\n\n/**\n" +
                " *                      江城子 . 程序员之歌\n" +
                " *\n" +
                " *                  十年生死两茫茫，写程序，到天亮。\n" +
                " *                      千行代码，Bug何处藏。\n" +
                " *                  纵使上线又怎样，朝令改，夕断肠。\n" +
                " *\n" +
                " *                  领导每天新想法，天天改，日日忙。\n" +
                " *                      相顾无言，惟有泪千行。\n" +
                " *                  每晚灯火阑珊处，夜难寐，加班狂。\n" +
                "*/\n" +
                "————————————————\n")
        stringBuilder.append("\n\n  ************错误日志***********\n\n")
        stringBuilder.append(writer.toString())
        return stringBuilder.toString()
    }

}