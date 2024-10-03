package com.dongyu.movies.network

import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.model.BingImageResponse
import kotlinx.coroutines.flow.flow

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
}