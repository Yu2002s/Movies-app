package com.wanban.screencast.listener

import com.wanban.screencast.model.DeviceModel

interface OnSearchedDevicesListener {
    fun onSearchedDevices(devices: List<DeviceModel>)
}