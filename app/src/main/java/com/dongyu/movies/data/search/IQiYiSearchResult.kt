package com.dongyu.movies.data.search

import com.google.gson.annotations.SerializedName

data class IQiYiSearchResult(
    val templates: List<Template>
)

data class Template(
    val template: Int,
    val albumInfo: AlbumInfo?
)

data class AlbumInfo(
    val title: String,
    val pageUrl: String,
    val videos: List<VideoInfo>,
    val year: Year?,
    @SerializedName("qipuId") val id: String
)

data class VideoInfo(
    val number: Int,
    val pageUrl: String,
)

data class Year(
    val value: Int?
)