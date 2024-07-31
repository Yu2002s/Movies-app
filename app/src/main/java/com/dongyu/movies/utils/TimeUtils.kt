package com.dongyu.movies.utils

import java.text.DateFormat
import java.util.Date

fun Long.getTime(): String {
    var timeStr = ""
    val second = this / 1000 % 60
    val minute = this / 1000 / 60 % 60
    val hour = this / 1000 / 3600 % 24
    timeStr += minute.fillZero() + ":" + second.fillZero()
    if (hour > 0) {
        timeStr = hour.fillZero() + ":" + timeStr
    }
    return timeStr
}

private val dateTimeFormat = DateFormat.getDateTimeInstance()

fun Long.getRelTime(): String {
    return dateTimeFormat.format(this)
}

fun Long.getDiffTime(): String {
    var timeStr = ""
    val second = this / 1000 % 60
    val minute = this / 1000 / 60 % 60
    val hour = this / 1000 / 3600 % 24
    if (hour > 0) {
        timeStr += "" + hour + "时"
    }
    if (minute > 0) {
        timeStr += "" + minute + "分"
    }
    timeStr += "" + second + "秒"
    return if (second >= 0) "+$timeStr" else timeStr
}

private fun Long.fillZero(): String {
    return if (this < 10) "0$this" else this.toString()
}