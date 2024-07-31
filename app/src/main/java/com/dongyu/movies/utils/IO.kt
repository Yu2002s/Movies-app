package com.dongyu.movies.utils

import java.util.concurrent.Executors

private val executor = Executors.newSingleThreadExecutor()

fun ioThread(block: () -> Unit) {
    executor.execute(block)
}