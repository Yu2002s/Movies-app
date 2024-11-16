package com.dongyu.movies.parser.impl

import android.util.Log
import androidx.annotation.Keep
import com.dongyu.movies.model.movie.MovieDetail
import com.dongyu.movies.model.movie.MovieItem
import com.dongyu.movies.model.movie.MovieVideo
import com.dongyu.movies.model.movie.VideoSource
import com.dongyu.movies.model.page.PageResult
import com.dongyu.movies.model.parser.ParserResult
import com.dongyu.movies.model.parser.PlayParam
import com.dongyu.movies.model.search.SearchData
import com.dongyu.movies.network.ParseRepository
import com.dongyu.movies.parser.JsonParser
import com.google.gson.annotations.SerializedName

/**
 * <p>小张影视</p>
 * <p><a href="https://v.nxux.cn/">小张影视</a></p>
 */
class XZParser : JsonParser() {

    @Keep
    data class SearchResponse(
        @SerializedName("d")
        val code: Int,
        @SerializedName("a")
        val data: List<SearchItem>,
        @SerializedName("c")
        val message: String,
    )

    @Keep
    data class SearchItem(
        val name: String,
        val link: String,
        val img: String
    )

    @Keep
    data class DetailResponse(
        val status: Int,
        @SerializedName("res")
        val data: DetailData
    )

    @Keep
    data class DetailData(
        val detail: Detail,
        val res: List<Source>
    )

    @Keep
    data class Detail(
        val name: String,
        val year: String,
        val img: String,
        val desc: String,
        val category: List<String>
    )

    @Keep
    data class Source(
        val name: String,
        val link: String
    )

    override fun parseSearchListJson(responseBody: String): ParserResult<SearchData>? {
        val body = responseBody.fromJson<SearchResponse>()

        if (body.code != 1) {
            return ParserResult.error("请尝试更换线路")
        }

        val regex = "\\./index\\.php\\?mode=detail&id=tv/(.+)".toRegex()

        return ParserResult.success(SearchData(
            PageResult(
                result = body.data.mapNotNull {
                    val id = regex.find(it.link)?.destructured?.component1()
                        ?: return@mapNotNull null
                    MovieItem().apply {
                        this.id = id
                        this.cover = it.img
                        this.tvName = it.name
                        this.star = "N/A"
                    }
                }
            ))
        )
    }

    override fun parseDetailJson(responseBody: String): ParserResult<MovieDetail>? {

        val detailResponse = responseBody.fromJson<DetailResponse>()
        if (detailResponse.status != 200) {
            return ParserResult.error("解析失败")
        }

        val detail = detailResponse.data.detail
        val movieItem = MovieItem().apply {
            tvName = detail.name
            cover = detail.img
            years = detail.year.split("-")[0]
            this.detail = detail.desc
            type = detail.category.joinToString(" ")
        }

        // 解析源列表(后台配置)
        val parseSourceList = ParseRepository.getParseSourceList().getOrThrow()

        val videoSources = parseSourceList.map {
            val items = detailResponse.data.res.mapIndexed { index, source ->
                VideoSource.Item(source.name, index, PlayParam("", it.id.toString(), source.link))
            }
            VideoSource(it.id.toString(), it.name, items)
        }
        val currentSourceItem = VideoSource.Item(videoSources.getOrNull(0)?.items?.getOrNull(0)!!)

        return ParserResult.success(MovieDetail(movieItem, currentSourceItem, videoSources))
    }

    override fun parseVideoJson(responseBody: String): ParserResult<MovieVideo>? {
        return super.parseVideoJson(responseBody)?.also {
            it.data.headers = mapOf(
                "User-Agent" to USER_AGENT,
            )
        }
    }

}