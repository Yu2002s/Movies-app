package com.dongyu.movies

import android.util.Base64
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
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test
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
        val document = Jsoup.connect("https://dianyi.ng")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
            .header("Cookie", "60b27e2f79149581c86727987ceaab5a=49a5553b38d3af2a02ff798ab85d5459")
            .get()

        println(document.html())
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