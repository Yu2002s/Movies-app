package com.dongyu.movies.api

import com.dongyu.movies.model.movie.MovieResponse
import com.dongyu.movies.model.base.BaseResponse
import com.dongyu.movies.model.movie.IQiYiVideoInfo
import com.dongyu.movies.model.movie.ParseSource
import com.dongyu.movies.model.search.DouBanSearchResult
import com.dongyu.movies.model.search.IQiYiSearchResult
import com.dongyu.movies.model.search.Suggest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface MovieService {

    /**
     * 获取所有影视
     */
    @GET("/movies")
    fun getMovieList(): Call<BaseResponse<List<MovieResponse.Movie>>>

    @GET("/movies/{id}")
    suspend fun getMovieById(@Path("id") id: Int): BaseResponse<MovieResponse.Movie>

    @GET
    fun searchIQIYIMovie(
        @Url url: String,
        @QueryMap map: Map<String, String>
    ): Call<BaseResponse<IQiYiSearchResult>>

    @GET
    fun getIQiYiVideo(
        @Url url: String,
        @Query("id") id: String,
    ): Call<IQiYiVideoInfo>

    @GET
    @Headers("Referer: https://www.douban.com/search")
    suspend fun searchDouBanMovie(
        @Url url: String,
        @Query("q") keyword: String,
        @Query("sort") sort: String,
    ): DouBanSearchResult

    /**
     * 获取主页的数据
     */
    @GET("/movies/home")
    suspend fun getHomeMovie(@Query("movieId") movieId: Int? = null): BaseResponse<MovieResponse.Movie>

    @GET("/movies/home_list")
    fun getHomeMoviesList(): Call<BaseResponse<List<MovieResponse.Movie>>>
}