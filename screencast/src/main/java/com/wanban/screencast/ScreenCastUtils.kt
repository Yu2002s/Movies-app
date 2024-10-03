package com.wanban.screencast

import android.app.Application
import android.util.Log
import com.wanban.screencast.base.BaseScreenCastUtils
import com.wanban.screencast.dlna.DLANUtils
import com.wanban.screencast.listener.OnDeviceConnectListener
import com.wanban.screencast.listener.OnSearchedDevicesListener
import com.wanban.screencast.listener.OnVideoProgressUpdateListener

object ScreenCastUtils {

    private const val screenCastType = BaseScreenCastUtils.TYPE_ALIYUN
    private val castUtils by lazy {
//        if (screenCastType == BaseScreenCastUtils.TYPE_ALIYUN) ALiScreenCastUtils() else DLANUtils()
        DLANUtils()
    }

    /**
     * 初始化
     */
    fun init(application: Application) {
//        AliPlayerScreenProjectionHelper.enableLog(true)
        castUtils.init(application)
    }

    /**
     * 扫描设备
     */
    fun startBrowser(listener: OnSearchedDevicesListener) {
        Log.e("ScreenCast", "startBrowser...")
        castUtils.startBrowser(listener)
    }

    fun stopBrowser() {
        castUtils.stopBrowser()
    }

    fun connectDevice(deviceName: String, listener: OnDeviceConnectListener) {
        Log.e("ScreenCast", "connectDevice...$deviceName")
        castUtils.connectDevice(deviceName, listener)
    }

    fun play(url: String, title: String?, listener: OnVideoProgressUpdateListener) {
        Log.e("ScreenCast", "play...$url")
        castUtils.play(url, title, listener)
    }

    fun pause() {
        Log.e("ScreenCast", "pause...")
        castUtils.pause()
    }

    fun resume() {
        castUtils.resume()
    }

    fun disconnect() {
        castUtils.disconnect()
    }

    fun stop() {
        Log.e("ScreenCast", "stop...")
        castUtils.stop()
    }

    fun setDuration(duration: Int){
        Log.e("ScreenCast", "setDuration...")
        castUtils.setDuration(duration)
    }


    fun seekTo(seekTo: Int) {
        Log.e("ScreenCast", "seekTo...")
        castUtils.seekTo(seekTo)
    }


    fun release() {
        Log.e("ScreenCast", "release...")
        castUtils.release()
    }

}