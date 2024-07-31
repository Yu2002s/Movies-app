package com.dongyu.movies.data.movie

import org.litepal.crud.LitePalSupport

data class Star(
    val name: String = "",
    val cover: String = "",
    val movieId: Long = 0,
    val routeId: Long = 0,
    val detailId: String = "",
    val createAt: Long
): LitePalSupport() {
    val id: Long = 0

    fun update() {
        update(id)
    }
}