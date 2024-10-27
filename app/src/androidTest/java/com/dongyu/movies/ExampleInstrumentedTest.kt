package com.dongyu.movies

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dongyu.movies.network.Repository
import com.dongyu.movies.parser.BaseParser
import com.dongyu.movies.utils.AESUtils
import com.dongyu.movies.utils.Md5Utils
import com.dongyu.movies.utils.base64ToHex
import com.dongyu.movies.utils.toHexString
import okhttp3.Request
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.IOException


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.dongyu.movies", appContext.packageName)
    }

    @Test
    fun getMd5() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        println(Build.CPU_ABI)
        // println(Checker.getAppSignatureMD5())
        // println(Checker.getAppSignatureMD5())
    }

    @Test
    fun testParse() {
        val pid = 183613
        val time = System.currentTimeMillis()
        val encryptStr = "$pid-$time"
        val key = Md5Utils.md5Hex(encryptStr)!!.substring(0, 16).toHexString()
        val encrypt = AESUtils.encrypt("AES/ECB/PkCS5Padding", key, encryptStr)

        val sign = encrypt?.base64ToHex()

        println("sign: $sign, time: $time , key: $key")

        val okhttp = Repository.okHttpClient
        val request = Request.Builder()
            .url("https://www.yjys.top/lines?t=${time}&sg=${sign}&pid=${pid}")
            .header("User-Agent", BaseParser.USER_AGENT)
            .header("Accept", BaseParser.ACCEPT)
            .header("Accept-Language", BaseParser.ACCEPT_LANGUAGE)
            .get()
            .build()
        val response = okhttp.newCall(request).execute()
        val responseBody = response.body()!!.string()
        println("body: $responseBody")
        var url = JSONObject(responseBody).getJSONObject("data").getString("m3u8")

        url = url.replace("bde4.cc", "yjys.top")

        println(url)

        val request2 = Request.Builder()
            .url(url)
            .header("User-Agent", BaseParser.USER_AGENT)
            .header("Accept", BaseParser.ACCEPT)
            .header("Accept-Language", BaseParser.ACCEPT_LANGUAGE)
            .get()
            .build()

        val response2 = okhttp.newCall(request2).execute()

        println("response: $response2")

        val bytes = response2.body()!!.byteStream().readBytes()
        val compressBytes = bytes.sliceArray(3354 until bytes.size)

        println(compressBytes.contentToString())
       /* val uncompressedBytes = test1(compressBytes)

        var result = ""
        var num = 16384
        for (i in 0 until uncompressedBytes.size) {
            result += uncompressedBytes.sliceArray((i * num).until((i + 1) * num))
        }

        println(result)*/
    }

}