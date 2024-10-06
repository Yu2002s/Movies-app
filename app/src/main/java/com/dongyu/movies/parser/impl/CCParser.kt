package com.dongyu.movies.parser.impl

import com.dongyu.movies.model.home.BannerItem
import com.dongyu.movies.model.home.CategoryData
import com.dongyu.movies.model.home.FilterData
import com.dongyu.movies.model.home.MainData
import com.dongyu.movies.model.home.MoviesCard
import com.dongyu.movies.model.home.NavItem
import com.dongyu.movies.model.movie.BaseMovieItem
import com.dongyu.movies.model.movie.MovieDetail
import com.dongyu.movies.model.movie.MovieItem
import com.dongyu.movies.model.movie.VideoSource
import com.dongyu.movies.model.page.PageResult
import com.dongyu.movies.model.parser.ParserResult
import com.dongyu.movies.parser.ParserList
import com.dongyu.movies.parser.SimpleParser
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * 策驰影视 <a href="https://www.hbeast.cn/">策驰</a>
 */
class CCParser: SimpleParser() {

    override fun parseSearchList(document: Document): ParserResult<PageResult<MovieItem>> {
        val pageResult = PageResult<MovieItem>()

        val row = requireNonNull(document.selectFirst(".container .row"))

        pageResult.result = row.select("#content .news-list li").map {
            val img = it.selectFirst(".img-pic")
            val href = img?.attr("href") ?: ""
            val id = getIdForHref(href)
            val title = img?.attr("title") ?: ""
            val status = img?.selectFirst(".text-right")?.text() ?: ""
            val cover = img?.selectFirst("img")?.attr("data-original") ?: ""
            val tips = it.select(".news-tips")
            val type = tips[0].selectFirst("a")?.text() ?: ""
            val star = tips[1].select("a").text()
            MovieItem().apply {
                this.id = id
                this.tvName = title
                this.status = status
                this.cover = cover
                this.type = type
                this.star = star
            }
        }

        getLastPage(row, pageResult)

        /*val href = row.selectFirst("#page .last a")?.attr("href")

        if (href == null) {
            pageResult.lastPage = 1
        } else {
            val regex = ".+-(\\d+)---\\.html".toRegex()
            regex.find(href)?.let {
                pageResult.lastPage = it.destructured.component1().toInt()
            }
        }*/

        return ParserResult.success(pageResult)
    }

    override fun parseDetail(document: Document): ParserResult<MovieDetail> {
        val detail = requireNonNull(document.selectFirst("body>.container .vod-detail"))
        val cover = detail.selectFirst(".vod-detail-pic img")?.attr("data-original") ?: ""

        val videoInfos = detail.select(".vod-detail-info li")
        val title = videoInfos[0].selectFirst("h1")?.text() ?: ""
        val star = videoInfos[2].select("a").text() ?: ""
        val status = videoInfos[3].ownText() ?: ""
        val director = videoInfos[4].ownText() ?: ""
        val type = videoInfos[5].selectFirst("a")?.text() ?: ""
        // val lang = videoInfos[6].ownText() ?: ""
        val area = videoInfos[7].ownText() ?: ""
        val year = videoInfos[8].ownText()
        val desc = videoInfos[10].text()

        val movieItem = MovieItem().apply {
            this.cover = cover
            tvName = title
            this.star = star
            this.status = status
            this.director = director
            this.type = type
            this.area = area
            this.years = year
            this.detail = desc
        }


        val row = requireNonNull(document.selectFirst("body>.container .row"))
        val sources = row.select(".tab-toggle a").map { it.text() }

        val sourceList = mutableListOf<VideoSource>()
        row.select(".play-list").mapIndexed { index, el ->
            val items = el.select("a").mapIndexed { i, item ->
                val playParam = requireNonNull(getPlayParamForUrl(item.attr("href")))
                VideoSource.Item(item.text(), i, playParam)
            }
            val id = items[0].param.sourceId
            val videoSource = VideoSource(id, sources[index], items)
            sourceList.add(videoSource)
        }

        val currentSourceItem = VideoSource.Item(sourceList[0].items[0])

        val movieDetail = MovieDetail(movieItem, currentSourceItem, sourceList)

        return ParserResult.success(movieDetail)
    }

    override fun parseVideo(document: Document?): ParserResult<String> {
        return ParserList.getParser(ParserList.MX_THEME.parseId).parseVideo(document)
    }

    override fun parseMain(document: Document): ParserResult<MainData> {
        val navItems = document.select(".header-nav-wrap li.swiper-slide a").map {
            val href = it.attr("href")
            val id = if (href == "/") {
                NavItem.HOME
            } else {
                getIdForHref(href)
            }
            NavItem(id, it.text())
        }.toMutableList()

        navItems.removeLast()
        navItems.removeLast()
        navItems.removeLast()

        val container = requireNonNull(document.selectFirst("body>.container"))
        val bannerItems = container.select(".swiper-wrapper .swiper-slide a").map {
            val href = it.attr("href")
            val id = getIdForHref(href)
            val name = it.attr("title")
            val cover = it.selectFirst("img")?.attr("data-src") ?: ""
            val status = it.selectFirst(".slide-text")?.text() ?: ""
            BannerItem(id, name, cover, status)
        }

        val tvList = getMovieCard(container)

        return ParserResult.success(MainData(navItems, bannerItems, tvList, emptyList()))
    }

    override fun parseClassify(
        document: Document,
        notParams: Boolean
    ): ParserResult<CategoryData> {

        val container = requireNonNull(document.selectFirst("body>.container"))

        var filterList: List<FilterData>? = null

        if (notParams) {
            val regex = "/vpooyj/(\\d+)".toRegex()
            val filterTypes = arrayOf(FilterData.FILTER_CATE, FilterData.FILTER_TYPE, FilterData.FILTER_AREA, FilterData.FILTER_YEAR)
            filterList = document.select(".type-box .type-select").mapIndexed { index, it ->
               val items = it.select("a")
                val name = items[0].text()
                val id = filterTypes[index]
                items.removeAt(0)
                val filterItems = items.map { item ->
                    val filterName = item.text()
                    val filterId = if (id == FilterData.FILTER_CATE) {
                        regex.find(item.attr("href"))?.destructured?.component1()
                    } else {
                        filterName
                    }
                    FilterData.Item(filterId ?: "", filterName, groupId = id)
                }
                FilterData(id, name, filterItems)
            }
        }

        val categoryList = getMovieItems(container)

        val pageResult = PageResult<BaseMovieItem>()
        pageResult.result = categoryList
        getLastPage(container, pageResult)

        return ParserResult.success(CategoryData(filterList, pageResult))
    }

    private fun getMovieCard(el: Element): List<MoviesCard<BaseMovieItem>> {
        return el.select(".layout-box").map {
            val title = it.selectFirst(".box-title h2")?.text() ?: ""
            val movies = it.select(".img-list li>a.img-pic").map { img ->
                val href = img.attr("href")
                val id = getIdForHref(href)
                val name = img.attr("title")
                val cover = requireNonNull(img.selectFirst("img")).attr("data-original")
                val status = img.selectFirst(".score")?.text() ?: ""
                BaseMovieItem(id, name, cover, status = status)
            }
            MoviesCard(title, movies)
        }
    }

    private fun getMovieItems(el: Element): List<BaseMovieItem> {
        return el.select(".layout-box .img-list li>a.img-pic").map { img ->
            val href = img.attr("href")
            val id = getIdForHref(href)
            val name = img.attr("title")
            val cover = requireNonNull(img.selectFirst("img")).attr("data-original")
            val status = img.selectFirst(".score")?.text() ?: ""
            BaseMovieItem(id, name, cover, status = status)
        }
    }

    private fun <T> getLastPage(el: Element, pageResult: PageResult<T>) {
        val href = el.selectFirst("#page .last a")?.attr("href")

        if (href == null) {
            pageResult.lastPage = 1
        } else {
            val regex = ".+-(\\d+)---\\.html".toRegex()
            regex.find(href)?.let {
                pageResult.lastPage = it.destructured.component1().toInt()
            }
        }
    }
}