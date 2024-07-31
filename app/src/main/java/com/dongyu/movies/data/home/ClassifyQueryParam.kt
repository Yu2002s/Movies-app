package com.dongyu.movies.data.home

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ClassifyQueryParam(
    @SerializedName("movieId")
    var queryMovieId: Int? = null,
    val cateId: Int = 1,
    var page: Int = 1,
    val type: String? = null,
    val area: String? = null,
    val year: String? = null,
    val sort: String? = null,
    val language: String? = null,
    @Expose(serialize = false, deserialize = false)
    var currentMovieId: Int = 1,
) {
    override fun toString(): String {
        return "ClassifyQueryParam(cateId=$cateId, page=$page, type='$type', area='$area', year=$year, sort='$sort', language='$language')"
    }
}
