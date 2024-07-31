package com.dongyu.movies

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dongyu.movies.utils.Checker

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

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
}