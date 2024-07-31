package com.dongyu.movies.network

import androidx.core.content.pm.PackageInfoCompat
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.base.BaseRepository
import com.dongyu.movies.base.requestCallResult

object AppRepository {

    private val appService = BaseRepository.appService()

    suspend fun checkUpdate() = requestCallResult {
        val context = MoviesApplication.context
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        val longVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
        appService.checkUpdate(longVersionCode)
    }

}