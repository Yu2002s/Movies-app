package com.dongyu.movies.network

import android.util.Log
import com.dongyu.movies.model.base.BaseResponse
import com.dongyu.movies.model.home.ClassifyQueryParam
import com.dongyu.movies.model.movie.ParseSource
import com.dongyu.movies.model.parser.ParseParam
import com.dongyu.movies.model.parser.PlayParam
import com.dongyu.movies.model.search.DouBanSearchResult
import com.dongyu.movies.model.search.History
import com.dongyu.movies.model.search.IQiYiSearchParams
import com.dongyu.movies.model.search.SearchParam
import com.dongyu.movies.model.search.SearchSuggestItem
import com.dongyu.movies.parser.ParserList
import com.dongyu.movies.parser.impl.DouBanParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONArray
import org.litepal.LitePal
import org.litepal.extension.find
import retrofit2.await
import java.io.IOException
import java.net.URLDecoder

/**
 * 视频仓库，获取视频信息
 */
object MovieRepository {

    private const val IQIYI_SEARCH_URL = "https://mesh.if.iqiyi.com/portal/lw/search/homePageV3"

    private const val IQIYI_VIDEO_URL = "https://mesh.if.iqiyi.com/player/pcw/video/baseInfo"

    // https://danmu.zxz.ee/?type=xml&id=
    private const val DAN_MA_KU_API = "https://dmku.hls.one/?ac=dm&url="

    private const val DOU_BNA_SEARCH_URL = "https://m.douban.com/rexxar/api/v2/search"

    private const val DOU_BAN_HOST = "https://movie.douban.com"

    private val movieService = Repository.movieService

    /**
     * 获取影视列表
     */
    suspend fun getMovieList() = requestCallResult { movieService.getMovieList() }

    /**
     * 通过唯一id获取影视实例
     */
    suspend fun getMovieById(movieId: Int) = requestResult { movieService.getMovieById(movieId) }

    /**
     * 获取影视详情
     */
    suspend fun getMovieDetail(parseParam: ParseParam) = requestParse {
        ParserList.getParser(parseParam.parseId)
            .setUrl(parseParam.parseUrl)
            .setDetailId(parseParam.detailId)
            .setSelectionId(parseParam.selectionId)
            .setSourceId(parseParam.sourceId)
            .detail
    }

    /**
     * 解析影视视频地址
     * @param playParam 播放所需的参数
     */
    suspend fun getMovieVideo(playParam: PlayParam) = requestParse {
        ParserList.getParser(playParam.parseId)
            .setPlayParam(playParam)
            .video
    }

    /**
     * 获取主页显示的影视信息
     * @param id 指定影视id
     */
    suspend fun getHomeMovie(id: Int? = null) = requestParse {
        val movie = movieService.getHomeMovie(id).data
        // 保存当前影视id
        // 首页中显示的影视是可变的，所以需要对后台返回的影视id进行保存
        Repository.currentMovie = movie
        ParserList.getParser(movie.parseId).setUrl(movie.host).main
    }

    /**
     * 获取影视的分类数据
     * @param param 需要过滤的一些参数
     */
    suspend fun getClassify(param: ClassifyQueryParam) = requestParse {
        val movie = Repository.getCurrentMovieAsync()
        ParserList.getParser(movie.parseId)
            .setClassifyQueryParam(param)
            .setUrl(movie.fullClassifyUrl)
            .classify
    }

    suspend fun getHomeMoviesList() = requestCallFlow {
        movieService.getHomeMoviesList()
    }

    suspend fun getSearchSuggest(name: String) = flowOf(runCatching {
        if (name.isBlank()) {
            LitePal.limit(20).order("updatedAt desc").find<History>()
                .map { SearchSuggestItem.Record(it) }
        } else {
            emptyList()
            /*val response = movieService.getSearchSuggest(name).await()
            if (response.code == 200) {
                response.data.map {
                    SearchSuggestItem.Item(it)
                }
            } else {
                throw Throwable(response.msg)
            }*/
        }
    })

    suspend fun getSearchTVList(searchParam: SearchParam) =
        requestParse {
            ParserList.getParser(searchParam.parseId)
                .setUrl(searchParam.searchUrl)
                .setVerifyUrl(searchParam.verifyUrl)
                .setName(searchParam.name)
                .setPage(searchParam.page)
                .setVerifyCode(searchParam.verifyCode)
                .searchList
        }

    /**
     * 通过影视名获取弹幕地址
     */
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

        // 先使用爱奇艺进行搜索
        var hasMovie = false

        try {
            val response = movieService.searchIQIYIMovie(IQIYI_SEARCH_URL, params).await()
            if (response.code != 0) {
                return@flow
            }

            for (template in response.data.templates) {
                val type = template.template
                val matchYear = template.albumInfo?.year?.value == searchParams.year
                if (searchParams.year != null && !matchYear) {
                    continue
                }

                if (type == 101 || type == 102) {
                    emit(template.albumInfo!!.videos.map {
                        DAN_MA_KU_API + it.pageUrl
                    })
                    hasMovie = true
                    break
                } else if (type == 103) {
                    val videoRes =
                        movieService.getIQiYiVideo(IQIYI_VIDEO_URL, template.albumInfo!!.id).await()
                    if (videoRes.code != "A00000") {
                        continue
                    }
                    emit(listOf(DAN_MA_KU_API + videoRes.data.playUrl))
                    hasMovie = true
                    break
                }
            }
        } catch (e: Exception) {
            Log.e("jdy", "getMovieDanMuKu: ${e.message}")
        }

        // 使用豆瓣进行搜索
        if (!hasMovie) {
            Log.d("jdy", "doubanSearch")
            try {
                val douBanSearchResult =
                    movieService.searchDouBanMovie(DOU_BNA_SEARCH_URL, searchParams.name, "relevance")
                Log.d("jdy", "response: $douBanSearchResult")
                val items = douBanSearchResult.subjects.items.toMutableList()
                douBanSearchResult.smartBox?.let {
                    items.addAll(it)
                }
                items.find {
                    if (searchParams.year != null) {
                        val matchYear = it.target.year == searchParams.year.toString()
                        if (!matchYear) {
                            return@find searchParams.name.replace(" ", "") ==
                                    it.target.title.replace(" ", "")
                        }
                        return@find true
                    } else {
                        searchParams.name.replace(" ", "") ==
                                it.target.title.replace(" ", "")
                    }
                }?.target_id?.let { id ->
                    Log.d("jdy", "targetId: $id")
                    // 使用解析器进行解析
                    val okhttp = Repository.okHttpClient
                    val request = Request.Builder()
                        .url("$DOU_BAN_HOST/subject/$id/")
                        .get()
                        .build()
                    val response = withContext(Dispatchers.IO) {
                        okhttp.newCall(request).execute().body()!!.string()
                    }
                    val regex = "\\{play_link: \"https://www\\.douban\\.com/link2/\\?url=(.+)%3F.+\", ep: \"(\\d)+\"\\}".toRegex()
                    emit(regex.findAll(response).map {
                        DAN_MA_KU_API + URLDecoder.decode(it.destructured.component1())
                    }.toList())

                }
            } catch (e: Exception) {
                Log.e("jdy", "getMovieDanmaku: $e")
            }
        }
    }

    suspend fun getDouBanSearchResult(name: String): DouBanSearchResult {
        return movieService.searchDouBanMovie(DOU_BNA_SEARCH_URL, name, "relevance")
    }
}