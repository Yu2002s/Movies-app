package com.dongyu.movies.api

import com.dongyu.movies.data.base.BaseResponse
import com.dongyu.movies.data.update.Update
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AppService {

    @GET("/apps/update")
    fun checkUpdate(@Query("code") code: Long): Call<BaseResponse<Update?>>

}