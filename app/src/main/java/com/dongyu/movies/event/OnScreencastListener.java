package com.dongyu.movies.event;

/**
 * 监听投屏状态
 */
public interface OnScreencastListener {

    /**
     * 已连接
     */
    void onConnected();

    /**
     * 投屏进度
     */
    void onProgress(Long current, Long duration);

    /**
     * 退出投屏
     */
    void onDisconnect();

}
