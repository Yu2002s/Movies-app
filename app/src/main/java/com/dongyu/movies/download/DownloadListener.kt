package com.dongyu.movies.download

import com.dongyu.movies.model.download.Download

interface DownloadListener {

    fun onDownload(download: Download)
}