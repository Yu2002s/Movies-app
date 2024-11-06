package com.dongyu.movies.api

import com.dongyu.movies.model.base.BaseResponse
import com.dongyu.movies.model.movie.LiveSource
import retrofit2.http.GET

interface LiveSourceService {

    @GET("/lives")
    suspend fun getSourceList(): BaseResponse<List<LiveSource>>

}