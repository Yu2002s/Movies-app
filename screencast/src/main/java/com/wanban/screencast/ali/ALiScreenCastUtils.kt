//package com.wanban.screencast.ali
//
//import android.content.Context
//import com.aliyun.player.aliplayerscreenprojection.AliPlayerScreenProjectionHelper
//import com.aliyun.player.aliplayerscreenprojection.Device
//import com.aliyun.player.aliplayerscreenprojection.Projection
//import com.aliyun.player.aliplayerscreenprojection.bean.AliPlayerScreenProjectDev
//import com.aliyun.player.aliplayerscreenprojection.bean.AliPlayerScreenProjectProState
//import com.aliyun.player.aliplayerscreenprojection.listener.AliPlayerScreenProjectDevListener
//import com.aliyun.player.aliplayerscreenprojection.listener.AliPlayerScreenProjectProListener
//import com.wanban.screencast.base.BaseScreenCastUtils
//import com.wanban.screencast.listener.OnDeviceConnectListener
//import com.wanban.screencast.listener.OnSearchedDevicesListener
//import com.wanban.screencast.listener.OnVideoProgressUpdateListener
//import com.wanban.screencast.model.DeviceModel
//
//
//class ALiScreenCastUtils: BaseScreenCastUtils(), AliPlayerScreenProjectDevListener {
//
//    private lateinit var deviceDelegate: Device
//    private lateinit var projectDelegate: Projection
//    private val devicesList by lazy { ArrayList<AliPlayerScreenProjectDev>() }
//
//
//    override fun getScreenCastType(): Int {
//        return TYPE_ALIYUN
//    }
//
//    override fun init(context: Context) {
//        //投屏设备相关对象
//        deviceDelegate = AliPlayerScreenProjectionHelper.createDeviceDelegate(context)
//        //投屏操作相关对象
//        projectDelegate = AliPlayerScreenProjectionHelper.createProjectDelegate(context)
//        // 注册监听
//        deviceDelegate.registerDeviceListener(this)
//
//
//    }
//
//    override fun startBrowser(listener: OnSearchedDevicesListener) {
//        super.startBrowser(listener)
//        projectDelegate.start(UserData)
//        deviceDelegate.searchDevices()
//    }
//
//    override fun stopBrowser() {
//        deviceDelegate.unRegisterDeviceListener()
//    }
//
//    override fun play(url: String, title: String?, listener: OnVideoProgressUpdateListener) {
//        progressListener = listener
//        projectDelegate.start(UserData)
//    }
//
//    override fun connectDevice(deviceName: String, listener: OnDeviceConnectListener) {
//        val device = devicesList.firstOrNull { it.name == deviceName }?:return
//
//        //注册监听
//        projectDelegate.registerProListener(object : AliPlayerScreenProjectProListener {
//            //当前播放进度回调
//            override fun onProgressUpdate(i: Int) {
//                progressListener?.onVideoProgressUpdate(i.toLong())
//            }
//
//            //投屏状态改变回调
//            override fun onStateChanged(aliPlayerScreenProjectProState: AliPlayerScreenProjectProState) {
//                if (aliPlayerScreenProjectProState == AliPlayerScreenProjectProState.PLAYING) {
//
//                }
//            }
//        })
//
//        deviceDelegate.selectDevice(device)
//        listener.onDeviceConnect()
//    }
//
//    override fun pause() {
//        projectDelegate.pause()
//    }
//
//    override fun seekTo(seek: Int) {
//        projectDelegate.seek(seek)
//    }
//
//
//    override fun setDuration(duration: Int){
//        projectDelegate.setDuration(duration)
//    }
//
//
//    override fun stop() {
//        devicesList.clear()
//        projectDelegate.stop()
//        projectDelegate.unRegisterProListener()
//        connectListener?.onDeviceDisConnect()
//    }
//
//    override fun release() {
//        deviceDelegate.unRegisterDeviceListener()
//        stop()
//    }
//
//
//    //开始搜索投屏设备回调
//    override fun onDevSearchStart() {
//
//    }
//    //搜索投屏设备完成回调
//    override fun onDevSearchStop() {
//        val searchResultList = deviceDelegate.devicesList
//        if (searchResultList.isEmpty()) return
//        val targetDeviceList = ArrayList<DeviceModel>()
//        devicesList.clear()
//        devicesList.addAll(searchResultList)
//        searchResultList.forEach {
//            val device = DeviceModel()
//            device.name = it.name
//            device.uuid = it.deviceUuid
//            targetDeviceList.add(device)
//        }
//        searchListener?.onSearchedDevices(targetDeviceList)
//    }
//
//
//}