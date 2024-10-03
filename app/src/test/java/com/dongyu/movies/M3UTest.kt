package com.dongyu.movies

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test

class M3UTest {

    @Test
    fun testM3U() {
        /*val okHttp = OkHttpClient()
        val request = Request.Builder()
            .url("https://iptv.b2og.com/o_cn.m3u")
            .build()

        val response = okHttp.newCall(request).execute()
*/
        val regex = "#EXTINF:-1 (.+)=\"(.+)\" (.+)=\"(.+)\" (.+)=\"(.+)\",(.+)\n(http://.+\\.m3u8)".toRegex()

        val content = "#EXTM3U\n" +
                "#EXTINF:-1 group-title=\"央视台\" tvg-id=\"CCTV-1综合\" tvg-logo=\"https://live.fanmingming.com/tv/CCTV1.png\",CCTV-1综合\n" +
                "http://121.24.98.226:8090/hls/9/index.m3u8\n" +
                "#EXTINF:-1 group-title=\"央视台\" tvg-id=\"CCTV-2财经\" tvg-logo=\"https://live.fanmingming.com/tv/CCTV2.png\",CCTV-2财经\n" +
                "http://121.24.98.226:8090/hls/10/index.m3u8"

        val list = regex.findAll(content)

        list.forEach {
            println(it.destructured.component1())
        }
    }

}