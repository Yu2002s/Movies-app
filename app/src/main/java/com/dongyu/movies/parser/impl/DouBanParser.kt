package com.dongyu.movies.parser.impl

import com.dongyu.movies.model.movie.DouBanMovieItem
import com.dongyu.movies.model.movie.MovieDetail
import com.dongyu.movies.model.movie.MovieItem
import com.dongyu.movies.model.movie.MovieVideo
import com.dongyu.movies.model.movie.VideoSource
import com.dongyu.movies.model.page.PageResult
import com.dongyu.movies.model.parser.ParserResult
import com.dongyu.movies.model.parser.PlayParam
import com.dongyu.movies.model.search.DouBanSearchResult
import com.dongyu.movies.model.search.SearchData
import com.dongyu.movies.network.MovieRepository
import com.dongyu.movies.network.ParseRepository
import com.dongyu.movies.parser.JsonParser
import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.net.URLDecoder

/**
 * 自用解析线路
 */
class DouBanParser : JsonParser() {

    override fun createSimpleConnection(url: String?): Connection {
        return super.createSimpleConnection(url).header("Referer", "${host}/search")
    }

    override fun parseSearchListJson(responseBody: String): ParserResult<SearchData> {

        val douBanSearchResult =
            GSON.fromJson(responseBody, DouBanSearchResult::class.java)

        val items = douBanSearchResult.subjects.items.toMutableList()
        douBanSearchResult.smartBox?.let {
            items.addAll(it)
        }

        val pageResult = PageResult(
            result = items.filter { it.target.has_linewatch }.map {
                MovieItem().apply {
                    id = it.target_id
                    tvName = it.target.title
                    cover = it.target.cover_url
                    val cardSubtitles = it.target.card_subtitle.split("/")
                    area = cardSubtitles[0]
                    type = cardSubtitles[1]
                    star = cardSubtitles.slice(2 until cardSubtitles.size).joinToString()
                    cate = if (it.target_type == "tv") "电视剧" else "电影"
                }
            }
        )

        return ParserResult.success(SearchData(pageResult))
    }

    override fun parseDetailJson(responseBody: String): ParserResult<MovieDetail>? {
        return null
    }

    override fun parseDetail(document: Document): ParserResult<MovieDetail> {

        val douBanMovieItem = document.selectFirst("script[type=application/ld+json]")?.data()?.run {
            GSON.fromJson(this, DouBanMovieItem::class.java)
        } ?: return ParserResult.error()

        val parseSourceList = ParseRepository.getParseSourceList().getOrThrow()

        val regex = "\\{play_link: \"https://www\\.douban\\.com/link2/\\?url=(.+)%3F.+\", ep: \"(\\d)+\"\\}".toRegex()

        val urls = regex.findAll(document.html()).map {
            URLDecoder.decode(it.destructured.component1())
        }.toList()
        val videoSources = parseSourceList.map {
            val items = urls.mapIndexed { index, url ->
                val selection = (index + 1).toString()
                VideoSource.Item("第${selection}集", index, PlayParam("", it.id.toString(), url))
            }
            VideoSource(it.id.toString(), it.name, items)
        }
        val currentSourceItem = VideoSource.Item(videoSources.getOrNull(0)?.items?.getOrNull(0)!!)

        return ParserResult.success(MovieDetail(MovieItem().apply {
            tvName = douBanMovieItem.name
            cover = douBanMovieItem.image
            years = douBanMovieItem.datePublished
            detail = douBanMovieItem.description
            director = douBanMovieItem.director.joinToString(",") { it.name }
            star = douBanMovieItem.actor.joinToString(",") { it.name }
        }, currentSourceItem, videoSources))
    }

    override fun parseVideoJson(responseBody: String): ParserResult<MovieVideo>? {
        return super.parseVideoJson(responseBody)?.also {
            it.data.headers = mapOf(
                "User-Agent" to USER_AGENT,
            )
        }
    }
}