package com.dongyu.movies.model

data class BingImageResponse(
    val images: List<Image>
)

data class Image(val url: String)