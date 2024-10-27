package com.dongyu.movies.parser

import android.util.Log
import com.dongyu.movies.model.base.BaseResponse
import com.dongyu.movies.model.movie.MovieDetail
import com.dongyu.movies.model.movie.MovieVideo
import com.dongyu.movies.model.parser.ParserResult
import com.dongyu.movies.model.search.SearchData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jsoup.nodes.Document
import kotlin.jvm.Throws

open class JsonParser : SimpleParser() {

    companion object {
        val GSON = Gson()
    }

    override fun getDetail(): ParserResult<MovieDetail> {
        val parseUrl = getParseUrl(url, TYPE.DETAIL)
        setParseUrl(parseUrl).setType(TYPE.DETAIL)
        val responseBody = getResponseBody(parseUrl) ?: return ParserResult.error(null)
        return parseDetailJson(responseBody) ?: super.getDetail()
    }

    open fun parseDetailJson(responseBody: String): ParserResult<MovieDetail>? {
        return ParserResult.success(getBaseResponse(responseBody))
    }

    override fun getSearchList(): ParserResult<SearchData> {
        val parseUrl = getParseUrl(url, TYPE.SEARCH)
        setParseUrl(parseUrl).setType(TYPE.SEARCH)
        val responseBody = getResponseBody(parseUrl) ?: return ParserResult.error(null)
        return parseSearchListJson(responseBody) ?: super.getSearchList()
    }

    open fun parseSearchListJson(responseBody: String): ParserResult<SearchData>? {
        return ParserResult.success(getBaseResponse(responseBody))
    }

    override fun getVideo(): ParserResult<MovieVideo> {
        val parseUrl = getParseUrl(url, TYPE.VIDEO)
        setParseUrl(parseUrl).setType(TYPE.VIDEO)
        val responseBody = getResponseBody(parseUrl) ?: return ParserResult.error(null)
        return parseVideoJson(responseBody) ?: super.getVideo()
    }

    open fun parseVideoJson(responseBody: String): ParserResult<MovieVideo>? {
        return ParserResult.success(getBaseResponse<MovieVideo>(responseBody))
    }

    @Throws(Exception::class)
    private inline fun <reified T> getBaseResponse(body: String): T {
        val response: BaseResponse<T> = GSON.fromJson(body, object : TypeToken<BaseResponse<T>>(){}.type)
        if (response.code != 200) throw Exception("response.code != 200")
        return response.data
    }
}