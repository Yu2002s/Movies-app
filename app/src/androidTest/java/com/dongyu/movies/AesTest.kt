package com.dongyu.movies;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.dongyu.movies.utils.AesEncryption
import org.junit.Test

import org.junit.runner.RunWith;
import java.nio.charset.StandardCharsets

class AesTest {

  @Test
  fun encryptTest() {
    val encrypt = AesEncryption.encrypt("12345678901234567890123456789012", "hello")
    println(encrypt)
    val decrypt = AesEncryption.decrypt("12345678901234567890123456789012", encrypt)
    println(decrypt)
  }
}
