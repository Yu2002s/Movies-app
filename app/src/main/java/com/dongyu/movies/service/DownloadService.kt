package com.dongyu.movies.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadService {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        val downloadJob = coroutineScope.launch {

        }

    }

}