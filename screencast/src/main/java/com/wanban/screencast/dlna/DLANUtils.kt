package com.wanban.screencast.dlna

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.wanban.screencast.ScreenCastUtils
import com.wanban.screencast.Utils
import com.wanban.screencast.base.BaseScreenCastUtils
import com.wanban.screencast.dlna.bean.DeviceInfo
import com.wanban.screencast.dlna.bean.MediaInfo
import com.wanban.screencast.dlna.listener.DLNAControlCallback
import com.wanban.screencast.dlna.listener.DLNADeviceConnectListener
import com.wanban.screencast.dlna.listener.DLNARegistryListener
import com.wanban.screencast.dlna.listener.DLNAStateCallback
import com.wanban.screencast.listener.OnDeviceConnectListener
import com.wanban.screencast.listener.OnSearchedDevicesListener
import com.wanban.screencast.listener.OnVideoProgressUpdateListener
import com.wanban.screencast.model.DeviceModel
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.meta.Service
import java.lang.ref.WeakReference

class DLANUtils : BaseScreenCastUtils() {

    private var mDLNAPlayer: DLNAPlayer? = null
    private lateinit var weakReference: WeakReference<Context>
    private var registryListener: MyDLNARegistryListener? = null
    private val deviceList by lazy { ArrayList<DeviceInfo>() }


    private val progressHandler by lazy { MyProgressHandler(mDLNAPlayer) }

    companion object {
        class MyProgressHandler(var mDLNAPlayer: DLNAPlayer?) : Handler(Looper.getMainLooper()),
            DLNAControlCallback {
            private var progressUpdateListener: OnVideoProgressUpdateListener? = null
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    1 -> {
                        mDLNAPlayer?.getPositionInfo(this)
                        sendProgressMsg(progressUpdateListener)
                    }
                }
            }

            fun sendProgressMsg(listener: OnVideoProgressUpdateListener?) {
                progressUpdateListener = listener
                removeMessages(1)
                sendEmptyMessageDelayed(1, 1000)
            }

            fun removeMessage() {
                removeCallbacksAndMessages(null)
            }

            private fun String.getTime(): Long {
                if (!TextUtils.isEmpty(this)) {
                    val timeArray = this.split(":")
                    var currentPosition = 0L
                    if (timeArray.size >= 3) {
                        // 秒
                        val s = timeArray[2]
                        val sFirst = s.subSequence(0, 1).toString().toInt()
                        if (sFirst > 0) {
                            currentPosition = sFirst * 10L
                        }
                        val sSecond = s.subSequence(1, 2).toString().toInt()
                        currentPosition += sSecond

                        // 分
                        val m = timeArray[1]
                        val mFirst = m.subSequence(0, 1).toString().toInt()
                        if (mFirst > 0) {
                            currentPosition = mFirst * 10 * 60L
                        }
                        val mSecond = m.subSequence(1, 2).toString().toInt()
                        currentPosition += mSecond * 60L

                        // 时
                        val h = timeArray[0]
                        if (h.length >= 2) {
                            val hFirst = h.subSequence(0, 1).toString().toInt()
                            if (hFirst > 0) {
                                currentPosition = hFirst * 10 * 60L
                            }
                            val hSecond = h.subSequence(1, 2).toString().toInt()
                            currentPosition += hSecond * 10 * 60 * 60L
                        } else {
                            val hFirst = h.toInt()
                            currentPosition += hFirst * 60 * 60L
                        }
                    } else {
                        // 秒
                        val s = timeArray[1]
                        val sFirst = s.subSequence(0, 1).toString().toInt()
                        if (sFirst > 0) {
                            currentPosition = sFirst * 10L
                        }
                        val sSecond = s.subSequence(1, 2).toString().toInt()
                        currentPosition += sSecond

                        // 分
                        val m = timeArray[0]
                        val mFirst = m.subSequence(0, 1).toString().toInt()
                        if (mFirst > 0) {
                            currentPosition = mFirst * 10 * 60L
                        }
                        val mSecond = m.subSequence(1, 2).toString().toInt()
                        currentPosition += mSecond * 60L
                    }
                    return currentPosition
                }
                return 0
            }

            override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {
                val time = invocation?.getOutput("AbsTime")?.toString() ?: "0:00:00"
                // Log.e("ScreenCast", "DLNA -> time: " + time)
                val duration = invocation?.getOutput("TrackDuration")?.toString() ?: "00:00:00"
                // 将String的time转成Long

                progressUpdateListener?.onVideoProgressUpdate(time.getTime() * 1000, duration.getTime() * 1000)
            }

            override fun onReceived(
                invocation: ActionInvocation<out Service<*, *>>?,
                vararg extra: Any?
            ) {

            }

            override fun onFailure(
                invocation: ActionInvocation<out Service<*, *>>?,
                errorCode: Int,
                errorMsg: String?
            ) {

            }
        }
    }

    override fun getScreenCastType(): Int {
        return TYPE_DLNA
    }

    /**
     * 初始化
     */
    override fun init(context: Context) {
        weakReference = WeakReference<Context>(context)
        initDLNA(context)
    }


    private inner class MyDLNARegistryListener : DLNARegistryListener() {
        override fun onDeviceChanged(deviceInfoList: MutableList<DeviceInfo>?) {
            val targetDeviceList = ArrayList<DeviceModel>()
            deviceList.clear()
            val list =
                deviceInfoList?.filter { "urn:schemas-upnp-org:device:MediaRenderer:1" == it.device?.type?.toString() }
            if (list.isNullOrEmpty()) {
                searchListener?.onSearchedDevices(targetDeviceList)
                return
            }
            deviceList.addAll(list)
            list.forEach {
                val device = DeviceModel()
                device.name = it.name
                device.uuid = it.mediaID
                targetDeviceList.add(device)
            }
            searchListener?.onSearchedDevices(targetDeviceList)
        }
    }

    /**
     * 扫描设备
     */
    override fun startBrowser(listener: OnSearchedDevicesListener) {
        super.startBrowser(listener)
        try {
            if (registryListener != null) {
                DLNAManager.getInstance().unregisterListener(registryListener)
            }
            registryListener = MyDLNARegistryListener()
            DLNAManager.getInstance().registerListener(registryListener)
            DLNAManager.getInstance().startBrowser(100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setDuration(duration: Int) {

    }

    override fun stopBrowser() {
        DLNAManager.getInstance().stopBrowser()
    }

    override fun connectDevice(deviceName: String, listener: OnDeviceConnectListener) {
        super.connectDevice(deviceName, listener)
        val deviceInfo = deviceList.firstOrNull { it.name == deviceName } ?: return
        if (mDLNAPlayer == null) {
            val context = weakReference.get() ?: return
            mDLNAPlayer = DLNAPlayer(context)
        }
        mDLNAPlayer?.setConnectListener(object : DLNADeviceConnectListener {
            override fun onConnect(deviceInfo: DeviceInfo, errorCode: Int) {
                Log.e("ScreenCast", "DLNA -> onConnect..." + deviceInfo.name)
                connectListener?.onDeviceConnect()
            }

            override fun onDisconnect(deviceInfo: DeviceInfo, type: Int, errorCode: Int) {
                Log.e("ScreenCast", "DLNA -> onDisconnect..." + deviceInfo.name)
                connectListener?.onDeviceDisConnect()
            }
        })
        mDLNAPlayer?.connect(deviceInfo)
    }


    override fun play(url: String, title: String?, listener: OnVideoProgressUpdateListener) {
        this.progressListener = listener
        val mediaInfo = MediaInfo()
        mediaInfo.mediaId = Base64.encodeToString(url.toByteArray(), Base64.NO_WRAP)
        mediaInfo.uri = url
        mediaInfo.mediaName = title
        mediaInfo.theAlbumName = title
        mediaInfo.mediaType = MediaInfo.TYPE_VIDEO
        mDLNAPlayer?.setDataSource(mediaInfo)
        mDLNAPlayer?.start(object : DLNAControlCallback {
            override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {
                progressHandler.sendProgressMsg(listener)
            }

            override fun onReceived(
                invocation: ActionInvocation<out Service<*, *>>?,
                vararg extra: Any?
            ) {
            }

            override fun onFailure(
                invocation: ActionInvocation<out Service<*, *>>?,
                errorCode: Int,
                errorMsg: String?
            ) {
            }
        })
    }

    override fun pause() {
        progressHandler.removeMessage()
        mDLNAPlayer?.pause(object : DLNAControlCallback {
            override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {
            }

            override fun onReceived(
                invocation: ActionInvocation<out Service<*, *>>?,
                vararg extra: Any?
            ) {
            }

            override fun onFailure(
                invocation: ActionInvocation<out Service<*, *>>?,
                errorCode: Int,
                errorMsg: String?
            ) {
            }
        })
    }

    override fun disconnect() {
        progressHandler.removeMessage()
    }

    override fun stop() {
        progressHandler.removeMessage()
        if (registryListener != null) {
            DLNAManager.getInstance().unregisterListener(registryListener)
        }
        mDLNAPlayer?.stop(object : DLNAControlCallback {
            override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {
            }

            override fun onReceived(
                invocation: ActionInvocation<out Service<*, *>>?,
                vararg extra: Any?
            ) {
            }

            override fun onFailure(
                invocation: ActionInvocation<out Service<*, *>>?,
                errorCode: Int,
                errorMsg: String?
            ) {
            }
        })

    }

    fun resume() {
        mDLNAPlayer?.play(object : DLNAControlCallback {
            override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {

            }

            override fun onReceived(
                invocation: ActionInvocation<out Service<*, *>>?,
                vararg extra: Any?
            ) {

            }

            override fun onFailure(
                invocation: ActionInvocation<out Service<*, *>>?,
                errorCode: Int,
                errorMsg: String?
            ) {

            }
        })
    }

//    override fun getCurrentPosition() {
//        mDLNAPlayer?.getPositionInfo(object : DLNAControlCallback {
//            override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {
//
//            }
//            override fun onReceived(invocation: ActionInvocation<out Service<*, *>>?, vararg extra: Any?) {
//            }
//            override fun onFailure(invocation: ActionInvocation<out Service<*, *>>?, errorCode: Int, errorMsg: String?) {
//            }
//        })
//    }


    override fun seekTo(seek: Int) {
        val seekString = Utils.getHMSTime(seek.toLong())
        Log.e("ScreenCast", "DLNA -> SeekTime: $seekString")
        mDLNAPlayer?.seekTo(seekString, object : DLNAControlCallback {
            override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {
            }

            override fun onReceived(
                invocation: ActionInvocation<out Service<*, *>>?,
                vararg extra: Any?
            ) {
            }

            override fun onFailure(
                invocation: ActionInvocation<out Service<*, *>>?,
                errorCode: Int,
                errorMsg: String?
            ) {

            }
        })
    }


    override fun release() {
        progressHandler.removeMessage()
        if (registryListener != null) {
            DLNAManager.getInstance().unregisterListener(registryListener)
        }
        registryListener = null
        DLNAManager.getInstance().stopBrowser()
        if (mDLNAPlayer != null) {
            mDLNAPlayer?.setConnectListener(null)
            mDLNAPlayer?.disconnect()
            mDLNAPlayer?.destroy()
            mDLNAPlayer = null
        }
        DLNAManager.getInstance().destroy()
    }

    private fun initDLNA(context: Context) {
        // 投屏
        DLNAManager.getInstance().init(context, object : DLNAStateCallback {
            override fun onConnected() {

            }

            override fun onDisconnected() {

            }
        })
    }


}