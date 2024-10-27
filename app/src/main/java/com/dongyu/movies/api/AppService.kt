package com.dongyu.movies.api

import com.dongyu.movies.model.BingImageResponse
import com.dongyu.movies.model.base.BaseResponse
import com.dongyu.movies.model.update.Update
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface AppService {

    @GET("/apps/update")
    fun checkUpdate(@Query("code") code: Long): Call<BaseResponse<Update?>>

    @GET
    suspend fun getSingleBingImage(@Url url: String): BingImageResponse

    @GET("/download/{key}")
    suspend fun getUpdateUrl(@Path("key") key: String): BaseResponse<Update.Download>
}