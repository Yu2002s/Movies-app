package com.dongyu.movies

import com.dongyu.movies.utils.Checker
import com.dongyu.movies.utils.EncryptUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.StandardCharsets

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


}

suspend fun main() {
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
}