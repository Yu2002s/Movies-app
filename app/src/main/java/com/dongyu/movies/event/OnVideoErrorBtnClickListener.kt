package com.dongyu.movies.event

/**
 * 视频播放错误时，按钮点击事件处理接口
 */
interface OnVideoErrorBtnClickListener {

    /**
     * 刷新
     */
    fun onRefreshClick()

    /**
     * 重载
     */
    fun onReloadClick()

    /**
     * 换源
     */
    fun onSwitchSourceClick()
}