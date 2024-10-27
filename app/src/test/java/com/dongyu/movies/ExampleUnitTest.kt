package com.dongyu.movies

import android.util.Base64
import android.util.Log
import com.dongyu.movies.utils.AESUtils
import com.dongyu.movies.utils.Checker
import com.dongyu.movies.utils.EncryptUtils
import com.dongyu.movies.utils.Md5Utils
import com.dongyu.movies.utils.toHexString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test() {

        /*val str = xor(byteArrayOf(-12, 66, 52, -74, 103, -73, 70, -80, -85),
            byteArrayOf(-98, 38, 77, -124, 87, -121, 116, -123)).toString(StandardCharsets.UTF_8)
        println(str)*/
       /*val a = EncryptUtils.getInstance().encode("23*hHVh5Ec0o4M4!9i9AEx@68&h9J\$MW")
        println(a)*/

        val str = " var sources = {};\n" +
                "            sources[8] = [\n" +
                "                        {play_link: \"https://www.douban.com/link2/?url=https%3A%2F%2Fm.bilibili.com%2Fbangumi%2Fplay%2Fep836299%3Fbsource%3Ddoubanh5&amp;subtype=8&amp;type=online-video\", ep: \"1\"},\n" +
                "                        {play_link: \"https://www.douban.com/link2/?url=https%3A%2F%2Fm.bilibili.com%2Fbangumi%2Fplay%2Fep836446%3Fbsource%3Ddoubanh5&amp;subtype=8&amp;type=online-video\", ep: \"2\"},\n" +
                "                        {play_link: \"https://www.douban.com/link2/?url=https%3A%2F%2Fm.bilibili.com%2Fbangumi%2Fplay%2Fep837088%3Fbsource%3Ddoubanh5&amp;subtype=8&amp;type=online-video\", ep: \"3\"},\n" +
                "            ];"

        val regex = "\\{play_link: \"https://www\\.douban\\.com/link2/\\?url=(.+)%3F.+\", ep: \"(\\d)+\"\\}".toRegex()

        val result = regex.find(str)
        println(result?.destructured?.component1())

    }

    private fun xor(bArr: ByteArray, bArr2: ByteArray): ByteArray {
        val length = bArr.size
        val length2 = bArr2.size
        var i = 0
        var i2 = 0
        while (i < length) {
            if (i2 >= length2) {
                i2 = 0
            }
            bArr[i] = (bArr[i].toInt() xor bArr2[i2].toInt()).toByte()
            i++
            i2++
        }
        return bArr
    }


    @Test
    fun testJsoup() {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://v.ddys.pro/v/movie/Alien.Romulus.2024.mp4")
            .get()
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
            .addHeader("Accept-Encoding", "identity;q=1, *;q=0")
            .addHeader("pragma", "no-cache")
            .addHeader("cache-control", "no-cache")
            .addHeader("sec-ch-ua-platform", "\"Windows\"")
            .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"")
            .addHeader("sec-ch-ua-mobile", "?0")
            .addHeader("origin", "https://ddys.mov")
            .addHeader("sec-fetch-site", "cross-site")
            .addHeader("sec-fetch-mode", "cors")
            .addHeader("sec-fetch-dest", "video")
            .addHeader("referer", "https://ddys.mov/")
            .addHeader("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
            .addHeader("range", "bytes=0-")
            .addHeader("priority", "i")
            .build()

        val response = client.newCall(request).execute()
        val stream = response.body()!!.byteStream()
        println(stream.read())
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun main() {
        try {
            // 示例输入
            val pid = 183613
            val time = 1728198837895

            println(Md5Utils.md5Hex("$pid-$time")!!.substring(0, 16).toHexString())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

}

/*suspend fun main() {
   //  val flow = MutableSharedFlow<Int>(replay = 0, extraBufferCapacity = 1, BufferOverflow.DROP_LATEST)
     val flow = MutableStateFlow(5)

    GlobalScope.launch {
        flow.collectLatest {
             delay(500)
            println("接收到了: $it")
        }
    }

    GlobalScope.launch {
        for (i in 0 until 3) {
             delay(200)
            flow.emit(i)
        }
    }


    delay(10000)
}*/