package com.dongyu.movies.api

import com.dongyu.movies.model.base.BaseResponse
import com.dongyu.movies.model.movie.ParseSource
import retrofit2.Call
import retrofit2.http.GET

interface ParseService {

    @GET("/parses")
    fun getParseSourceList(): Call<BaseResponse<List<ParseSource>>>
}