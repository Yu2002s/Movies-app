package com.dongyu.movies.network

import com.dongyu.movies.base.BaseRepository
import com.dongyu.movies.base.requestCallFlow
import com.dongyu.movies.base.requestCallResult
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.data.home.ClassifyQueryParam
import com.dongyu.movies.utils.SpUtils.get

object HomeRepository {

    private val homeService = BaseRepository.homeService()

    suspend fun getMain() = requestCallFlow {
        val id = SPConfig.CURRENT_ROUTE_ID.get<Int>(-1)
        homeService.getMain(if (id == -1) null else id)
    }

    suspend fun getClassify(param: ClassifyQueryParam) = requestCallResult {
        val id = SPConfig.CURRENT_ROUTE_ID.get<Int>(-1)
        param.queryMovieId = if (id == -1) null else id
        homeService.getClassify(param)
    }

    suspend fun getHomeMoviesList() = requestCallFlow {
        homeService.getHomeMoviesList()
    }
}