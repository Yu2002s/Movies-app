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
import org.jsoup.nodes.Document

/**
 * 低端影视专用解析器
 * @href https://ddys.mov/
 * 发布页:  https://ddys.info
 */
class DDParser : SimpleParser() {

    companion object {
        private const val COVER =
            "https://gips2.baidu.com/it/u=1651586290,17201034&fm=3028&app=3028&f=JPEG&fmt=auto&q=100&size=f600_800"

        private const val VIDEO_HOST = "https://v.ddys.pro"
    }

    private val headers = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
        "Accept-Encoding" to "identity;q=1, *;q=0",
        "pragma" to "no-cache",
        "cache-control" to "no-cache",
        "sec-ch-ua-platform" to "\"Windows\"",
        "sec-ch-ua" to "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"",
        "sec-ch-ua-mobile" to "?0",
        "origin" to "https://ddys.mov",
        "sec-fetch-site" to "cross-site",
        "sec-fetch-mode" to "cors",
        "sec-fetch-dest" to "video",
        "referer" to "https://ddys.mov/",
        "accept-language" to "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
        "range" to "bytes=0-",
        "priority" to "i",
    )

    private fun getId(href: String): String {
        val regex = "/(.+)/$".toRegex()
        return regex.find(href)?.destructured?.component1() ?: ""
    }

    override fun parseSearchList(document: Document): ParserResult<SearchData> {
        val movieItems = document.select("article .row").map {
            MovieItem().apply {
                val a = requireNonNull(it.selectFirst(".post-title a"))
                id = getId(a.attr("href"))
                cover = COVER
                tvName = a.text()
                type = requireNonNull(it.selectFirst(".entry-content")).text()
                years = requireNonNull(it.selectFirst(".entry-date")).text()
            }
        }

        return ParserResult.success(SearchData(pageResult = PageResult(result = movieItems)))
    }

    override fun parseDetail(document: Document): ParserResult<MovieDetail> {
        val data = requireNonNull(document.selectFirst("script.wp-playlist-script")).data()

        val detail = document.selectFirst(".entry .doulist-item .doulist-subject")
        val movieItem = MovieItem()
        detail?.let {
            val cover = requireNonNull(it.selectFirst(".post img")).attr("src")
            val title = requireNonNull(it.selectFirst(".title")).text()
            val score = requireNonNull(it.selectFirst(".rating")).text().toFloatOrNull() ?: 0f
            val abstract = requireNonNull(it.selectFirst(".abstract"))
            val nodes =  abstract.textNodes()
            movieItem.run {
                this.cover = cover
                this.tvName = title
                this.score = score
                if (nodes.size > 1) {
                    director = nodes[1].text()
                }
                if (nodes.size > 2) {
                    star = nodes[2].text()
                }
                if (nodes.size > 3) {
                    type = nodes[3].text()
                }
                if (nodes.size > 4) {
                    area = nodes[4].text()
                }
                if (nodes.size > 5) {
                    years = nodes[5].text()
                }
                if (nodes.size > 6) {
                    this.detail = nodes[6].text()
                }
            }
        }

        val regex = ",\"src0\":\"(.+)\",\"src1\"".toRegex()
        val videoSources = mutableListOf<VideoSource>()
        val items = regex.findAll(data).mapIndexed { index, it ->
            val url = VIDEO_HOST + it.destructured.component1().replace("\\", "")
            val selection = (index + 1).toString()
            VideoSource.Item(
                selection,
                index,
                PlayParam(detailId = detailId, sourceId = "", selectionId = selection),
                url
            )
        }.toList()
        val currentSourceItem = if (selectionId.isEmpty()) {
            VideoSource.Item(items[0])
        } else {
            VideoSource.Item(items[selectionId.toInt() - 1])
        }

        videoSources.add(VideoSource("", "默认", items))

        return ParserResult.success(
            MovieDetail(
                movieItem = movieItem,
                currentSourceItem = currentSourceItem,
                videoSources = videoSources,
                video = MovieVideo(url = currentSourceItem.url!!, headers)
            )
        )
    }
}