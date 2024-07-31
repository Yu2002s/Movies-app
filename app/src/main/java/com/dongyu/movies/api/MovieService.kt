package com.dongyu.movies.api

import com.dongyu.movies.data.movie.MovieResponse
import com.dongyu.movies.data.base.BaseResponse
import com.dongyu.movies.data.movie.IQiYiVideoInfo
import com.dongyu.movies.data.movie.MovieDetail
import com.dongyu.movies.data.movie.Video
import com.dongyu.movies.data.search.IQiYiSearchResult
import org.jsoup.Connection.Base
import retrofit2.Call
import retrofit2.http.GET
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

    /**
     * 获取某个影视详情信息
     * @param id 影视id
     */
    @GET("/movies/{id}")
    fun getMovieDetail(
        @Path("id") id: Int,
        @Query("detailId") detailId: String,
        @Query("routeId") routeId: Int,
        @Query("selection") selection: Int
    ): Call<BaseResponse<MovieDetail>>

    @GET("/movies/video/{id}")
    fun getMovieVideo(
        @Path("id") id: Int,
        @Query("detailId") detailId: String,
        @Query("routeId") routeId: Int,
        @Query("selection") selection: Int
    ): Call<BaseResponse<Video>>

    @GET
    fun getMovieDanMuKu(
        @Url url: String,
        @QueryMap map: Map<String, String>
    ): Call<BaseResponse<IQiYiSearchResult>>

    @GET
    fun getIQiYiVideo(
        @Url url: String,
        @Query("id") id: String,
    ): Call<IQiYiVideoInfo>
}