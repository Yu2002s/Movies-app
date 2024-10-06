package com.dongyu.movies.utils

import android.content.Intent
import android.os.Parcelable
import android.util.Base64
import android.widget.Toast
import com.dongyu.movies.MoviesApplication

inline fun <reified T> startActivity(vararg args: Pair<String, Any>) {
  val context = MoviesApplication.context
  context.startActivity(Intent(context, T::class.java).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    args.forEach {
      when (val second = it.second) {
        is String -> putExtra(it.first, second)
        is Int -> putExtra(it.first, second)
        is Float -> putExtra(it.first, second)
        is Parcelable -> putExtra(it.first, second)
      }
    }
  })
}

fun String?.showToast(duration: Int = Toast.LENGTH_SHORT) {
  if (this == null) return
  Toast.makeText(MoviesApplication.context, this, duration).show()
}

fun formatBytes(bytes: Long): String {
  val units = arrayOf("B", "KB", "MB", "GB", "TB")
  if (bytes == 0L) return "0 B"
  var currentBytes = bytes.toDouble()
  var unitIndex = 0

  while (currentBytes >= 1024 && unitIndex < units.size - 1) {
    currentBytes /= 1024.0
    unitIndex++
  }

  return "%.2f %s".format(currentBytes, units[unitIndex])
}

fun String.isUrl(): Boolean {
  if (this.isEmpty()) {
    return false
  }
  val url = this.trim()
  return url.matches("^(http|https)://.+".toRegex())
}

fun String.toHexString(): String {
  return this.map {
    "%02X".format(it.code)
  }.joinToString("").lowercase()
}

fun String.base64ToHex(): String {
  // 将Base64字符串解码为字节数组
  val decodedBytes: ByteArray = Base64.decode(this, Base64.DEFAULT)
  // 将字节数组转换为十六进制字符串
  val hexString = StringBuilder()
  for (b in decodedBytes) {
    // 将每个字节转换为两位十六进制数
    val hex = Integer.toHexString(0xff and b.toInt())
    if (hex.length == 1) {
      hexString.append('0') // 如果是一位，则在前面补0
    }
    hexString.append(hex)
  }
  return hexString.toString()
}