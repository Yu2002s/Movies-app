package com.dongyu.movies.utils

import java.util.concurrent.Executors
import java.util.concurrent.Future

private val executor = Executors.newSingleThreadExecutor()

fun ioThread(block: () -> Unit) {
    executor.execute(block)
}