package com.dongyu.movies.utils

import android.content.Intent
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