package com.wanban.screencast.base

import android.content.Context
import com.wanban.screencast.listener.OnDeviceConnectListener
import com.wanban.screencast.listener.OnSearchedDevicesListener
import com.wanban.screencast.listener.OnVideoProgressUpdateListener

abstract class BaseScreenCastUtils {

    companion object {
        const val UserData = "WanbanPlayer"
        const val TYPE_DLNA = 0
        const val TYPE_ALIYUN = 1
    }

    protected var searchListener: OnSearchedDevicesListener? = null
    protected var progressListener: OnVideoProgressUpdateListener? = null
    protected var connectListener: OnDeviceConnectListener? = null


    abstract fun getScreenCastType(): Int

    abstract fun init(context: Context)

    open fun startBrowser(listener: OnSearchedDevicesListener) {
        searchListener = listener
    }
    abstract fun setDuration(duration: Int)

    abstract fun stopBrowser()

    open fun connectDevice(deviceName: String, listener: OnDeviceConnectListener) {
        connectListener = listener
     }

    abstract fun play(url: String, title: String?, listener: OnVideoProgressUpdateListener)

    abstract fun pause()

    abstract fun seekTo(seek: Int)

//    abstract fun getCurrentPosition(): Int

    abstract fun stop()

    abstract fun release()
}