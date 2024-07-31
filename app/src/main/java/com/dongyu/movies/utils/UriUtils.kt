package com.dongyu.movies.utils

import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.dongyu.movies.MoviesApplication
import java.io.File

fun String.toFileUri(): Uri {
    val context = MoviesApplication.context
    val pkgName = context.packageName
    return FileProvider.getUriForFile(context, "$pkgName.fileProvider", File(this))
}