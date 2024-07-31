package com.dongyu.movies.api;

import com.dongyu.movies.data.base.BaseResponse
import com.dongyu.movies.data.home.CategoryData
import com.dongyu.movies.data.home.ClassifyQueryParam
import com.dongyu.movies.data.home.MainData
import com.dongyu.movies.data.movie.MovieResponse
import org.jetbrains.annotations.Nullable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET;
import retrofit2.http.POST
import retrofit2.http.Query

interface HomeService {

    /**
     * 获取主页的数据
     */
    @GET("/homes")
    fun getMain(@Query("movieId") movieId: Int? = null): Call<BaseResponse<MainData>>

    @POST("/homes")
    fun getClassify(
        @Body classifyQueryParam: ClassifyQueryParam
    ): Call<BaseResponse<CategoryData>>

    @GET("/homes/list")
    fun getHomeMoviesList(): Call<BaseResponse<List<MovieResponse.Movie>>>
}
