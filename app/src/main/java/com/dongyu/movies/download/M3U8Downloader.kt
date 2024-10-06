package com.dongyu.movies.download

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.R
import com.dongyu.movies.model.download.Download
import com.dongyu.movies.utils.isUrl
import org.litepal.LitePal
import org.litepal.extension.findFirst
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.spec.AlgorithmParameterSpec
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * M3U8下载器
 */
class M3U8Downloader(
    private val executorThreadPool: ExecutorService,
    val download: Download
) {

    companion object {

        private const val TAG = "M3U8Downloader"

    }

    private var downloadTask: M3U8DownloadTask? = null

    fun download() {
        // 开始下载
        executorThreadPool.execute {
            downloadTask = M3U8DownloadTask(download)
            downloadTask?.startDownload()
        }
    }

    fun pause() {
        downloadTask?.pauseDownload()
    }

    fun resume() {
        downloadTask?.resumeDownload()
    }

    fun stop() {
        downloadTask?.stopDownload()
    }

    /**
     * M3U8下载任务
     */
    class M3U8DownloadTask(
        private var download: Download,
    ) {

        companion object {

            /**
             * 重试次数
             */
            private const val RETRY_COUNT = 2

            private val TEMP_DIR = MoviesApplication.context.externalCacheDir!!.path + "/temp"

            private val DEFAULT_OUTPUT_DIR =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)!!.path + "/DongYuMovies"
        }

        /**
         * 下载线程池
         * 单次10线程下载
         */
        private val downloadThreadPool = ThreadPoolExecutor(
            10, 10,
            0L, TimeUnit.MILLISECONDS,
            LinkedBlockingQueue()
        )

        //链接连接超时时间（单位：毫秒）
        private val timeoutMillisecond = 1000

        /**
         * 临时存储ts文件目录
         */
        private val tsTempFile get() = "$TEMP_DIR/$fileName"

        //合并后的文件存储目录
        private val outputDir: String
            get() {
                val groupName = download.groupName
                if (groupName.isNotEmpty()) {
                    return "$DEFAULT_OUTPUT_DIR/$groupName"
                }
                return DEFAULT_OUTPUT_DIR
            }

        //合并后的视频文件名称
        private var fileName: String = ""
            get() {
                if (field.isBlank()) {
                    field = System.currentTimeMillis().toString()
                }
                return field
            }
            set(value) {
                field = value.replace("[\\\\/:*?\"<>|\\s]", "_")
            }

        /**
         * 下载地址
         */
        private val url get() = download.url

        //已完成ts片段个数
        private var finishedCount = 0

        //解密算法名称
        private var method: String? = null

        //密钥
        private var key = ""

        //密钥字节
        private var keyBytes = ByteArray(16)

        //key是否为字节
        private var isByte = false

        //IV
        private var iv = ""

        //所有ts片段下载链接
        private val tsUrlSet: LinkedHashSet<String> = LinkedHashSet()

        //解密后的片段
        private val finishedFiles: MutableSet<File> =
            ConcurrentSkipListSet(Comparator.comparingInt { o: File ->
                // 将文件进行升序排序用于合并文件
                o.name.replace(".ts", "").toInt()
            })

        //已经下载的文件大小
        private var downloadBytes = 0L

        //自定义请求头
        // private val requestHeaderMap: Map<String, Any> = HashMap()

        /**
         * 暂停状态
         */
        private var isPause = false

        private var isStop = false

        private val lockObj = Object()

        fun stopDownload() {
            isStop = true
            finishedFiles.clear()
            tsUrlSet.clear()
            downloadThreadPool.shutdownNow()
            // 注意，这里会删除文件
            File(tsTempFile).deleteRecursively()
        }

        fun pauseDownload() {
            isPause = true
            download.pause()
        }

        fun resumeDownload() {
            isPause = false
            download.start()
            synchronized(lockObj) {
                lockObj.notifyAll()
            }
        }

        fun startDownload() {
            try {
                val _download =
                    LitePal.where("url = ?", download.url).limit(1).findFirst<Download>()
                if (_download != null) {
                    // 绑定事件监听
                    _download.listener = download.listener
                    download = _download
                    fileName = download.name
                    // 这里未进行判断，可能进行重复下载
                } else {
                    if (download.name.isEmpty()) {
                        download.name = fileName
                    } else {
                        fileName = download.name
                    }
                    download.downloadPath = "$outputDir/$fileName.mp4"
                    download.save()
                }

                download.updateAt = System.currentTimeMillis()

                download.prepare()

                getTsUrl()

                // 生成一些必须的文件夹
                generateFile()

                download.start()

                tsUrlSet.forEachIndexed { index, ts ->
                    downloadThreadPool.execute {
                        downloadFile(ts, index)
                    }
                }

                getDownloadStatus()
                handleCompleteDownload()

                Log.i(TAG, "downloadBytesCompleted: $downloadBytes Byte")
            } catch (e: Exception) {
                // 获取文件信息失败
                Log.e(TAG, e.toString())
                download.error()
            } finally {
                stopDownload()
            }
        }

        /**
         * 阻塞获取下载状态
         */
        private fun getDownloadStatus() {
            downloadThreadPool.shutdown()
            while (!downloadThreadPool.isTerminated && !isStop) {
                if (!isPause) {
                    // 一秒更新一次
                    Thread.sleep(1000)
                    download.currentByte = downloadBytes
                    download.progress = finishedCount * 100 / tsUrlSet.size
                }
            }
        }

        @SuppressLint("MissingPermission")
        @Throws(Exception::class)
        private fun handleCompleteDownload() {
            if (isStop) {
                return
            }
            // 可能部分ts会出现下载失败的情况，这里直接进行判断下载数量不为0则为成功
            if (finishedCount == 0) {
                Log.e(TAG, "$fileName 下载失败")
                throw Exception("下载失败")
            }
            download.progress = 100
            // finishedCount 不为0则代表成功
            download.merge()
            mergeTs()
            // 删除不需要的临时文件
            deleteTempFiles()
            download.completed()
            Log.i(TAG, "$fileName 合并完成")
        }

        private fun generateFile() {
            // 创建临时文件
            val tempFile = File(tsTempFile)
            if (!tempFile.exists())
                tempFile.mkdirs()

            //如果生成目录不存在，则创建
            val file = File(outputDir)
            if (!file.exists()) file.mkdirs()
        }

        /**
         * 删除下载好的片段
         */
        private fun deleteTempFiles() {
            File(tsTempFile).delete()
            tsUrlSet.clear()
            finishedFiles.clear()
        }

        /**
         * 合并下载好的ts片段
         */
        private fun mergeTs() {
            try {
                val file = File(download.downloadPath)
                System.gc()
                if (file.exists()) file.delete()
                else file.createNewFile()
                val fileOutputStream = FileOutputStream(file)
                val b = ByteArray(4096)
                for (f in finishedFiles) {
                    val fileInputStream = FileInputStream(f)
                    var len: Int
                    while ((fileInputStream.read(b).also { len = it }) != -1) {
                        fileOutputStream.write(b, 0, len)
                    }
                    fileInputStream.close()
                    fileOutputStream.flush()
                }
                fileOutputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                // 合并ts文件出错了
                Log.e(TAG, "$fileName 合并ts文件出错")
            }
        }

        private fun downloadFile(tsUrl: String, index: Int) {
            var count = 1
            var httpURLConnection: HttpURLConnection? = null

            //xy为未解密的ts片段，如果存在，则删除
            val unDecryptFile = File("$tsTempFile/$index.ts")
            if (unDecryptFile.exists()) unDecryptFile.delete()
            var outputStream: OutputStream? = null
            var decryptFileOutStream: FileOutputStream? = null
            val bytes = ByteArray(40960)

            //重试次数判断
            while (!isStop && !isPause && count <= RETRY_COUNT) {
                try {
                    if (count != 1) {
                        // 延迟两秒等待
                        Thread.sleep(2000)
                    }
                    //模拟http请求获取ts片段文件
                    httpURLConnection = createHttpURLConnection(tsUrl)
                    val inputStream = httpURLConnection.inputStream

                    try {
                        outputStream = FileOutputStream(unDecryptFile)
                    } catch (e: FileNotFoundException) {
                        // 可能出现文件异常，直接进行下一循环
                        Log.e(TAG, "文件不存在，跳过 $index")
                        e.printStackTrace()
                        continue
                    }
                    var len: Int
                    //将未解密的ts片段写入文件
                    while ((inputStream.read(bytes).also { len = it }) != -1) {
                        outputStream.write(bytes, 0, len)
                        synchronized(this) {
                            // 记录已下载的字节数
                            downloadBytes += len
                        }
                        if (isPause) {
                            synchronized(lockObj) {
                                lockObj.wait()
                            }
                        }
                        if (isStop) {
                            break
                        }
                    }

                    if (isStop) {
                        break
                    }

                    outputStream.flush()

                    val length = inputStream.available()
                    val decryptBytes = decrypt(bytes, length, key, iv, method)

                    if (decryptBytes != null) {
                        // 替换原本的文件
                        decryptFileOutStream = FileOutputStream(unDecryptFile)
                        decryptFileOutStream.write(decryptBytes)
                    }

                    inputStream.close()
                    finishedFiles.add(unDecryptFile)
                    break
                } catch (e: Exception) {
                    if (e is InvalidKeyException || e is InvalidAlgorithmParameterException) {
                        Log.e(TAG, "解密失败！")
                        // 文件解密失败了
                        break
                    }
                    if (e is InterruptedException) {
                        break
                    }
                    e.printStackTrace()
                    Log.e(TAG, "错误: ${e}, 第" + count + "获取链接重试！\t" + tsUrl)
                    count++
                } finally {
                    try {
                        // decryptFileInputStream?.close()
                        decryptFileOutStream?.close()
                        outputStream?.close()
                        // BLOCKING_QUEUE.put(bytes)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    httpURLConnection?.disconnect()
                }
            }
            if (isStop || isPause) {
                return
            }
            if (count <= RETRY_COUNT) {
                finishedCount++
            } else {
                Log.e(TAG, "tsFile $tsUrl 下载失败")
            }
        }

        /**
         * 根据url创建http连接对象
         */
        private fun createHttpURLConnection(tsUrl: String): HttpURLConnection {
            //模拟http请求获取ts片段文件
            val url = URL(tsUrl)
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.connectTimeout = timeoutMillisecond
            httpURLConnection.useCaches = false
            httpURLConnection.readTimeout = timeoutMillisecond
            httpURLConnection.doInput = true
            return httpURLConnection
        }

        /**
         * 获取所有的ts片段下载链接
         *
         * @return 链接是否被加密，null为非加密
         */
        @Throws(Exception::class)
        private fun getTsUrl(): String? {
            val content: StringBuilder = getUrlContent(url, false)
            //判断是否是m3u8链接
            if (!content.toString()
                    .contains("#EXTM3U")
            ) throw Exception(url + "不是m3u8链接！")
            val split = content.toString().split("\\n".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            var keyUrl = ""
            var isKey = false
            for (s in split) {
                //如果含有此字段，则说明只有一层m3u8链接
                if (s.contains("#EXT-X-KEY") || s.contains("#EXTINF")) {
                    isKey = true
                    keyUrl = url
                    break
                }
                //如果含有此字段，则说明ts片段链接需要从第二个m3u8链接获取
                if (s.contains(".m3u8")) {
                    if (s.isUrl()) return s
                    val relativeUrl: String =
                        url.substring(0, url.lastIndexOf("/") + 1)
                    var s2 = s
                    if (s.startsWith("/")) s2 = s.replaceFirst("/".toRegex(), "")
                    keyUrl = mergeUrl(relativeUrl, s2)
                    break
                }
            }
            if (TextUtils.isEmpty(keyUrl)) throw Exception("未发现key链接！")
            //获取密钥
            val key1: String? = if (isKey) getKey(keyUrl, content) else getKey(keyUrl, null)
            if (!key1.isNullOrEmpty()) key = key1
            else return null
            return key
        }

        /**
         * 获取ts解密的密钥，并把ts片段加入set集合
         *
         * @param url     密钥链接，如果无密钥的m3u8，则此字段可为空
         * @param content 内容，如果有密钥，则此字段可以为空
         * @return ts是否需要解密，null为不解密
         */
        @Throws(Exception::class)
        private fun getKey(url: String, content: StringBuilder?): String? {
            val urlContent =
                if (content.isNullOrEmpty()) getUrlContent(
                    url,
                    false
                )
                else content
            if (!urlContent.toString()
                    .contains("#EXTM3U")
            ) throw Exception(url + "不是m3u8链接！")
            val split = urlContent.toString().split("\\n".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (s in split) {
                //如果含有此字段，则获取加密算法以及获取密钥的链接
                if (s.contains("EXT-X-KEY")) {
                    val split1 = s.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    for (s1 in split1) {
                        if (s1.contains("METHOD")) {
                            method = s1.split("=".toRegex(), limit = 2).toTypedArray()[1]
                            continue
                        }
                        if (s1.contains("URI")) {
                            key = s1.split("=".toRegex(), limit = 2).toTypedArray()[1]
                            continue
                        }
                        if (s1.contains("IV")) iv =
                            s1.split("=".toRegex(), limit = 2).toTypedArray()[1]
                    }
                }
            }
            val relativeUrl = url.substring(0, url.lastIndexOf("/") + 1)
            //将ts片段链接加入set集合
            var i = 0
            while (i < split.size) {
                val s = split[i]
                if (s.contains("#EXTINF")) {
                    val s1 = split[++i]
                    tsUrlSet.add(if (s1.isUrl()) s1 else mergeUrl(relativeUrl, s1))
                }
                i++
            }
            if (!TextUtils.isEmpty(key)) {
                key = key.replace("\"", "")
                return getUrlContent(
                    if (key.isUrl()) key else mergeUrl(
                        relativeUrl,
                        key
                    ), true
                ).toString().replace("\\s+".toRegex(), "")
            }
            return null
        }

        /**
         * 模拟http请求获取内容
         *
         * @param urls  http链接
         * @param isKey 这个url链接是否用于获取key
         * @return 内容
         */
        private fun getUrlContent(urls: String, isKey: Boolean): StringBuilder {
            var count = 1
            var httpURLConnection: HttpURLConnection? = null
            val content = StringBuilder()
            while (count <= RETRY_COUNT) {
                try {
                    val url = URL(urls)
                    httpURLConnection = url.openConnection() as HttpURLConnection
                    httpURLConnection.connectTimeout = timeoutMillisecond
                    httpURLConnection.readTimeout = timeoutMillisecond
                    httpURLConnection.useCaches = false
                    httpURLConnection.doInput = true
                    /*for ((key, value) in requestHeaderMap.entries) {
                        httpURLConnection.addRequestProperty(key, value.toString())
                    }*/
                    var line: String?
                    val inputStream = httpURLConnection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    if (isKey) {
                        val bytes = ByteArray(128)
                        val len = inputStream.read(bytes)
                        isByte = true
                        if (len == 1 shl 4) {
                            keyBytes = bytes.copyOf(16)
                            content.append("isByte")
                        } else content.append(String(bytes.copyOf(len)))
                        return content
                    }
                    while ((bufferedReader.readLine().also { line = it }) != null) content.append(
                        line
                    ).append("\n")
                    bufferedReader.close()
                    inputStream.close()
                    // Log.i(TAG, content.toString())
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "第" + count + "获取链接重试！\t" + urls)
                    count++
                    e.printStackTrace();
                } finally {
                    httpURLConnection?.disconnect()
                }
            }
            if (count > RETRY_COUNT) throw Exception("连接超时！")
            return content
        }

        /**
         * 解密ts
         *
         * @param sSrc   ts文件字节数组
         * @param length
         * @param sKey   密钥
         * @return 解密后的字节数组
         */
        @OptIn(ExperimentalStdlibApi::class)
        @Throws(Exception::class)
        private fun decrypt(
            sSrc: ByteArray,
            length: Int,
            sKey: String,
            iv: String,
            method: String?
        ): ByteArray? {
            if (!method.isNullOrEmpty() && !method.contains("AES")) throw Exception("未知的算法！")
            // 判断Key是否正确
            if (sKey.isEmpty()) return null
            // 判断Key是否为16位
            if (sKey.length != 16 && !isByte) {
                throw Exception("Key长度不是16位！")
            }
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val keySpec = SecretKeySpec(
                if (isByte) keyBytes else sKey.toByteArray(),
                "AES"
            )
            var ivByte: ByteArray
            ivByte = if (iv.startsWith("0x")) iv.substring(2).hexToByteArray()
            else iv.toByteArray()
            if (ivByte.size != 16) ivByte = ByteArray(16)
            //如果m3u8有IV标签，那么IvParameterSpec构造函数就把IV标签后的内容转成字节数组传进去
            val paramSpec: AlgorithmParameterSpec = IvParameterSpec(ivByte)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec)
            return cipher.doFinal(sSrc, 0, length)
        }

        private fun mergeUrl(start: String, end: String): String {
            var endVar = end
            if (endVar.startsWith("/")) endVar = endVar.replaceFirst("/".toRegex(), "")
            var position = 0
            var subVar: String
            var tempVar = endVar
            while ((endVar.indexOf("/", position).also { position = it }) != -1) {
                subVar = endVar.substring(0, position + 1)
                if (start.endsWith(subVar)) {
                    tempVar = endVar.replaceFirst(subVar.toRegex(), "")
                    break
                }
                ++position
            }
            return start + tempVar
        }
    }

}