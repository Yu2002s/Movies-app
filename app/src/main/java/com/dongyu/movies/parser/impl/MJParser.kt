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
import com.dongyu.movies.parser.SimpleParser
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * 美剧天堂
 * <a href="http://www.mjtt5.tv">美剧天堂</a>
 */
class MJParser : SimpleParser() {

    companion object {

        private val ID_REGEX = "(\\w+)/$".toRegex()

        private val SOURCE_REGEX = "/\\w+/(\\w+)/(\\d+)-(\\d+)\\.html".toRegex()

        private val VIDEO_REGEX = "var ff_urls='(.+)';".toRegex()
    }

    private fun parseId(href: String): String {
        return ID_REGEX.find(href)?.destructured?.component1() ?: ""
    }

    private fun parsePlayParam(href: String): PlayParam? {
        val matchResult = SOURCE_REGEX.find(href) ?: return null
        return PlayParam(
            matchResult.destructured.component1(), matchResult.destructured.component2(),
            matchResult.destructured.component3()
        )
    }

    override fun parseSearchList(document: Document): ParserResult<SearchData>? {
        val pageResult = PageResult<MovieItem>()

        val panel = requireNonNull(document.selectFirst(".container:nth-child(2) .row .z-pannel"))
        pageResult.result = panel.select(".z-pannel_bd li a.z-list_pic").map {
            val href = it.attr("href")
            val id = parseId(href)
            val title = it.attr("title")
            val cover = it.attr("data-original")
            val status = it.selectFirst(".z-status")?.text() ?: ""
            val type = it.selectFirst(".z-type")?.text() ?: ""
            MovieItem().apply {
                this.id = id
                this.tvName = title
                this.cover = cover
                this.status = status
                this.type = type
            }
        }

        panel.selectFirst(".z-page storage")?.text()?.let {
            val regex = "共(\\d+)部&nbsp;(\\d+)/(\\d+)".toRegex()
            regex.find(it)?.let { page ->
                pageResult.total = page.destructured.component1().toInt()
                pageResult.currentPage = page.destructured.component2().toInt()
                pageResult.lastPage = page.destructured.component3().toInt()
            }
        }

        return ParserResult.success(SearchData(pageResult))
    }

    override fun parseDetail(document: Document): ParserResult<MovieDetail> {
        val panels = document.select(".container .row .z-pannel")
        val cover = panels[0].selectFirst(".z-list_pic")?.attr("data-original") ?: ""

        val movieItem = MovieItem()
        panels[0].selectFirst(".col-xs-6")?.let { el ->
            with(movieItem) {
                this.cover = cover
                val title = el.selectFirst("h1")?.text() ?: ""
                this.tvName = title
            }
        }

        val currentSourceItem: VideoSource.Item?
        val sources = mutableListOf<VideoSource>()
        for (i in (if (panels.size > 5) 2 else 1) until (panels.size - (if (panels.size > 5)  4 else 3))) {
            val title = panels[i].selectFirst(".z-pannel_title")?.text() ?: "默认"
            var sourceId: String
            val sourceItems = panels[i].select(".z-vod_list a").mapIndexed { index, it ->
                val href = it.attr("href")
                val playParam = parsePlayParam(href)!!

                VideoSource.Item(it.text(), index, playParam)
            }
            sourceId = sourceItems.getOrNull(0)?.param?.sourceId ?: ""
            val videoSource = VideoSource(sourceId, title, sourceItems)
            sources.add(videoSource)
        }

        currentSourceItem = sources.getOrNull(0)?.items?.getOrNull(0)

        if (currentSourceItem == null) {
            return ParserResult.error("解析源失败")
        }

        return ParserResult.success(
            MovieDetail(
                movieItem = movieItem,
                currentSourceItem = currentSourceItem,
                videoSources = sources
            )
        )
    }

    override fun parseVideo(document: Document): ParserResult<MovieVideo>? {

        val html = document.html()

        val matchResult = VIDEO_REGEX.find(html)
        val json = matchResult?.destructured?.component1() ?: return ParserResult.error("解析失败")
        val obj = JSONObject(json)

        var url: String?
        try {
            val dataObj = obj.getJSONObject("Data")
            val source = dataObj.getJSONObject(sourceId)
            val playName = source.getString("playname")
            val urls = source.getJSONArray("playurls")

            url = urls.getJSONArray(selectionId.toInt() - 1).getString(1)

            url = parsePlayUrl(playName, url)
        } catch (e: Exception) {
            val source = obj.getJSONArray("Data").getJSONObject(sourceId.toInt())
            val playName = source.getString("playname")
            url = source
                .getJSONArray("playurls")
                .getJSONArray(selectionId.toInt() - 1)
                .getString(1)
            url = parsePlayUrl(playName, url)
        }

        return ParserResult.success(MovieVideo(requireNonNull(url, "解析视频地址失败")))
    }

    private fun parsePlayUrl(playerName: String, url: String): String {
        var regexStr = ""
        val requestUrl: String = when (playerName) {
            "huobo" -> {
                regexStr = "\"url\": \"(.+)\""
                "https://php.playerla.com/mjplay/?id=$url"
            }
            "juhe" -> {
                regexStr = "quality: (.+),"
                "https://php.playerla.com/mjdplay/super.php?id=$url"
            }
            else -> return url
        }
        val playerDoc = Jsoup.connect(requestUrl)
            .header("Referer", "https://www.mjtt5.tv/")
            .get()

        val regex = regexStr.toRegex()

        when (playerName) {
            "huobo" -> {
                return regex.find(playerDoc.html())?.destructured?.component1() ?: url
            }
            "juhe" -> {
                val matchResult = regex.find(playerDoc.html())?: return url
                val quality  = matchResult.destructured.component1()
                val jsonArray = JSONArray(quality)
                return jsonArray.getJSONObject(jsonArray.length() - 1).getString("url")
            }
        }
        return url
    }
}