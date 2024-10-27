package com.dongyu.movies.network

import androidx.core.content.pm.PackageInfoCompat
import com.dongyu.movies.MoviesApplication

object AppRepository {

    private val appService = Repository.appService

    suspend fun checkUpdate() = requestCallResult {
        val context = MoviesApplication.context
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        val longVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
        appService.checkUpdate(longVersionCode)
    }

    suspend fun getBingImage() = requestSuspendSimpleFlow {
        appService.getSingleBingImage("https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1")
    }

    suspend fun getUpdateUrl(key: String) = requestResult {
        appService.getUpdateUrl(key)
    }
}