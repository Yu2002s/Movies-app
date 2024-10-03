package com.dongyu.movies.model.update

import java.util.Date

data class Update(
    val versionName: String,
    val versionCode: Long,
    val size: String,
    val url: String?,
    val content: String,
    val altUrl: String,
    val updateAt: Date
)