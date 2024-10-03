package com.dongyu.movies.model.search

import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

data class History(
    @Column(unique = true, nullable = false)
    val name: String,
    val updatedAt: Long
): LitePalSupport()