package com.dongyu.movies.network

import android.util.Log
import com.dongyu.movies.base.BaseRepository
import com.dongyu.movies.base.requestCallFlow
import com.dongyu.movies.base.requestCallResult
import com.dongyu.movies.data.movie.PlayParam
import com.dongyu.movies.data.search.IQiYiSearchParams
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import retrofit2.await

/**
 * 视频仓库，获取视频信息
 */
object MovieRepository {

    private const val IQIYI_SEARCH_URL = "https://mesh.if.iqiyi.com/portal/lw/search/homePageV3"

    private const val IQIYI_VIDEO_URL = "https://mesh.if.iqiyi.com/player/pcw/video/baseInfo"

    private const val DAN_MA_KU_API = "https://danmu.zxz.ee/?type=xml&id="

    private val movieService = BaseRepository.movieService()

    suspend fun getMovieList() = requestCallResult { movieService.getMovieList() }

    suspend fun getMovieDetail(playParam: PlayParam) =
        requestCallFlow {
            movieService.getMovieDetail(
                playParam.id,
                playParam.detailId,
                playParam.routeId,
                playParam.selection
            )
        }

    suspend fun getMovieVideo(playParam: PlayParam) = requestCallFlow {
        movieService.getMovieVideo(
            playParam.id,
            playParam.detailId,
            playParam.routeId,
            playParam.selection
        )
    }

    suspend fun getMovieDanMuKu(searchParams: IQiYiSearchParams) = flow {
        val params =
            mapOf(
                "key" to searchParams.name,
                "version" to "12.63.16605",
                "pageNum" to "1",
                "pageSize" to "5",
                "u" to "073d9531c0df5b7257d32a2b1f661a35",
                "mode" to "1",
                "source" to "input",
                "scale" to "125",
                "userVip" to "0",
                "vipType" to "-1",
            )
        try {
            val response = movieService.getMovieDanMuKu(IQIYI_SEARCH_URL, params).await()
            Log.d("jdy", "response: $response")
            if (response.code != 0) {
                return@flow
            }

            for (template in response.data.templates) {
                val type = template.template
                val matchYear = template.albumInfo?.year?.value == searchParams.year
                if (!matchYear) {
                    continue
                }

                if (type == 101 || type == 102) {
                    emit(template.albumInfo!!.videos.map {
                        DAN_MA_KU_API + it.pageUrl
                    })
                    break
                } else if (type == 103) {
                    val videoRes = movieService.getIQiYiVideo(IQIYI_VIDEO_URL, template.albumInfo!!.id).await()
                    Log.d("jdy", "videoResponse: $videoRes")
                    if (videoRes.code != "A00000") {
                        continue
                    }
                    emit(listOf(DAN_MA_KU_API + videoRes.data.playUrl))
                    break
                }
            }
        } catch (e: Exception) {
            Log.e("jdy", "getMovieDanMuKu: ${e.message}")
        }
    }
}