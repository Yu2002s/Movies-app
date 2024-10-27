package com.dongyu.movies.parser.impl

import android.util.Log
import com.dongyu.movies.model.movie.MovieDetail
import com.dongyu.movies.model.movie.MovieItem
import com.dongyu.movies.model.movie.MovieVideo
import com.dongyu.movies.model.movie.VideoSource
import com.dongyu.movies.model.page.PageResult
import com.dongyu.movies.model.parser.ParserResult
import com.dongyu.movies.model.parser.PlayParam
import com.dongyu.movies.model.search.SearchData
import com.dongyu.movies.model.search.VerifyData
import com.dongyu.movies.network.Repository
import com.dongyu.movies.parser.BaseParser
import com.dongyu.movies.parser.SimpleParser
import com.dongyu.movies.utils.AESUtils
import com.dongyu.movies.utils.Md5Utils
import com.dongyu.movies.utils.base64ToHex
import com.dongyu.movies.utils.toHexString
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.nodes.Document

/**
 * 缘觉影视
 */
class YJParser : SimpleParser() {

    companion object {
        private val PLAY_PARAM_REGEX = "([a-z]+)?/play/(\\d+)-(\\d+).htm".toRegex()
    }

    override fun getParseUrl(url: String?, type: TYPE): String {
        if (type == TYPE.SEARCH && verifyCode != null) {
            return super.getParseUrl("$url?code=${verifyCode}", type)
        }
        return super.getParseUrl(url, type)
    }

    override fun getPlayParamForUrl(href: String): PlayParam? {
        val matchResult = PLAY_PARAM_REGEX.find(href) ?: return null
        return PlayParam(
            detailId = matchResult.destructured.component2(),
            sourceId = matchResult.destructured.component1(),
            selectionId = matchResult.destructured.component3()
        )
    }

    override fun parseSearchList(document: Document): ParserResult<SearchData>? {

        val body = requireNonNull(document.selectFirst(".page-body"))

        val verifyImg = body.selectFirst("img.input-group-text")

        if (verifyImg != null) {
            // 需要验证
            val imgUrl = host + verifyImg.attr("src")
            val bytes = getResponseBytes(imgUrl)
            return ParserResult.success(SearchData(verifyData = VerifyData(codeBytes = bytes)))
        }

        val total = requireNonNull(body.selectFirst(".text-success")).text().toInt()

        val movieItems = body.select(".row-cards .row").mapNotNull { row ->
            val card = row.selectFirst(".card-body") ?: return@mapNotNull null
            val movieItem = MovieItem()
            val items = card.select(".mb-0").map { it.ownText() }
            if (items.isEmpty()) {
                return@mapNotNull null
            }
            movieItem.apply {
                director = items.getOrNull(1) ?: ""
                star = items.getOrNull(3) ?: ""
                type = items.getOrNull(4) ?: ""
                area = items.getOrNull(5) ?: ""
            }
            movieItem.cover = row.selectFirst(".object-cover")?.attr("src") ?: ""
            card.selectFirst(".search-movie-title")?.let {
                val href = it.attr("href")
                movieItem.id = href.substring(1)
                movieItem.tvName = it.attr("title")
            }
            movieItem
        }

        val pageResult = PageResult(result = movieItems, total = total)

        body.selectFirst(".card .pagination")?.let {
            it.selectFirst(".page-item:last-child a")?.attr("href")?.let { page ->
                val regex = "(\\d+)$".toRegex()
                pageResult.lastPage = regex.find(page)?.destructured?.component1()?.toInt() ?: 1
            }
        } ?: { pageResult.lastPage = 1 }

        return ParserResult.success(SearchData(pageResult = pageResult))
    }

    override fun parseDetail(document: Document): ParserResult<MovieDetail> {
        val cards = document.select(".container-xl>.card")
        val rows = cards[0].select(".row")
        val name = rows[0].selectFirst(".d-sm-block")?.text() ?: ""
        val cover = rows[1].selectFirst(".cover-lg-max-25>img")?.attr("src") ?: ""

        val items = rows[1].select(".mb-0 a")
        val movieItem = MovieItem().apply {
            tvName = name
            this.cover = cover
            director = items[0].text()
            star = items[2].text()
            type = items[3].text()
            area = items[4].text()
        }

        movieItem.detail = document.selectFirst("#synopsis .card-body")?.text() ?: ""

        val sourceItems = cards[1].select("#play-list a").mapIndexedNotNull { index, it ->
            val playParam = getPlayParamForUrl(it.attr("href")) ?: return@mapIndexedNotNull null
            VideoSource.Item(it.text(), index, playParam)
        }
        val currentSourceItem = VideoSource.Item(sourceItems[0])
        val videoSource = VideoSource(currentSourceItem.param.sourceId, "默认", sourceItems)

        return ParserResult.success(
            MovieDetail(
                movieItem = movieItem,
                currentSourceItem = currentSourceItem,
                listOf(videoSource)
            )
        )
    }

    override fun parseVideo(document: Document): ParserResult<MovieVideo> {

        val regex = "var pid = (\\d+);".toRegex()

        val pid = regex.find(document.html())?.destructured?.component1() ?: return ParserResult.error("解析失败")
        val time = System.currentTimeMillis()
        val encryptStr = "$pid-$time"
        val key = Md5Utils.md5Hex(encryptStr)!!.substring(0, 16).toHexString()
        val encrypt = AESUtils.encrypt("AES/ECB/PkCS5Padding", key, encryptStr)

        val sign = encrypt?.base64ToHex()

        // println("sign: $sign, time: $time , key: $key")

        val okhttp = Repository.okHttpClient
        val request = Request.Builder()
            .url("https://www.yjys.top/lines?t=${time}&sg=${sign}&pid=${pid}")
            .header("User-Agent", USER_AGENT)
            .header("Accept", ACCEPT)
            .header("Accept-Language", ACCEPT_LANGUAGE)
            .get()
            .build()
        val response = okhttp.newCall(request).execute()
        val responseBody = response.body()!!.string()
        Log.d(TAG, "response: $responseBody")
        val url: String
        try {
            url = JSONObject(responseBody).getJSONObject("data").getString("url3")
        } catch (ignored: Exception) {
            return ParserResult.error("请切换其他线路播放[BD]")
        }
        // println("body: $responseBody")

        return ParserResult.success(MovieVideo(url))
    }
}