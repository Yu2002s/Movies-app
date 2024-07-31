package com.dongyu.movies.api

import com.dongyu.movies.data.base.BaseResponse
import com.dongyu.movies.data.search.SearchResult
import com.dongyu.movies.data.search.Suggest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchService {

    /**
     * 查询搜索建议
     */
    @GET("/searchs/suggest")
    fun getSearchSuggest(@Query("name") name: String): Call<BaseResponse<List<Suggest>>>

    /**
     * 获取影视下搜索到的视频
     * @param id 影视ID
     * @param name 搜索关键字
     */
    @GET("/searchs/{id}")
    fun getSearchTVList(
        @Path("id") id: Int,
        @Query("name") name: String,
        @Query("page") page: Int
    ): Call<BaseResponse<SearchResult>>
}