package com.dongyu.movies.api

import com.dongyu.movies.model.movie.MovieResponse
import com.dongyu.movies.model.base.BaseResponse
import com.dongyu.movies.model.home.CategoryData
import com.dongyu.movies.model.home.ClassifyQueryParam
import com.dongyu.movies.model.movie.IQiYiVideoInfo
import com.dongyu.movies.model.search.IQiYiSearchResult
import com.dongyu.movies.model.search.Suggest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
    fun getMovieDanMuKu(
        @Url url: String,
        @QueryMap map: Map<String, String>
    ): Call<BaseResponse<IQiYiSearchResult>>

    @GET
    fun getIQiYiVideo(
        @Url url: String,
        @Query("id") id: String,
    ): Call<IQiYiVideoInfo>

    /**
     * 获取主页的数据
     */
    @GET("/movies/home")
    suspend fun getHomeMovie(@Query("movieId") movieId: Int? = null): BaseResponse<MovieResponse.Movie>

    @GET("/movies/home_list")
    fun getHomeMoviesList(): Call<BaseResponse<List<MovieResponse.Movie>>>

    /**
     * 查询搜索建议
     */
    @GET("/movies/search_suggest")
    fun getSearchSuggest(@Query("name") name: String): Call<BaseResponse<List<Suggest>>>
}