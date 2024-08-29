package com.wanban.screencast.dlna.listener;

import com.wanban.screencast.dlna.bean.DeviceInfo;

import java.util.List;

public interface OnBrowserDevicesChangedListener {
    public void onBrowserDevicesChanged(List<DeviceInfo> deviceInfoList);
}
