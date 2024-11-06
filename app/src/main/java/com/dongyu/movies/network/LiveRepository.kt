package com.dongyu.movies.network

import com.dongyu.movies.model.movie.LiveSourceItem
import okhttp3.Request

object LiveRepository {

    /**
     * 直播源列表获取网址
     */
    // IPV6: https://iptv.b2og.com/y_g.m3u
    const val LIVE_M3U_HOST = "https://iptv.b2og.com/y_g.m3u"

    private val okHttpClient = Repository.okHttpClient

    private val REGEX =
        "#EXTINF:-1 (.+),(.+)\n(.+\\.m3u8)".toRegex()

    private val PARAM_REGEX = "(.+)=\"(.+)\"".toRegex()

    private val liveSourceService = Repository.liveSourceService

    /**
     * 获取直播源列表
     */
    suspend fun getSourceList() = requestFlow {
        liveSourceService.getSourceList()
    }

    fun getSourceItemList() = getSourceItemList(LIVE_M3U_HOST)

    /**
     * 获取直播源下的item列表
     */
    fun getSourceItemList(source: String) = requestSimpleFlow {
        val request = Request.Builder()
            .url(source)
            .build()

        val response = okHttpClient.newCall(request).execute()

        REGEX.findAll(response.body()!!.string()).map {
            val param = it.destructured.component1()
            var groupTitle = ""
            var id = ""
            var logo = ""
            PARAM_REGEX.findAll(param).forEach { p ->
                val value = p.destructured.component2()
                when (p.destructured.component1()) {
                    "group-title" -> groupTitle = value
                    "tvg-id", "tvg-name" -> id = value
                    "tvg-logo" -> logo = value
                }
            }

            val title = it.destructured.component2()
            val url = it.destructured.component3()
            LiveSourceItem(id, title, groupTitle, logo, url)
        }.toList()
    }
}