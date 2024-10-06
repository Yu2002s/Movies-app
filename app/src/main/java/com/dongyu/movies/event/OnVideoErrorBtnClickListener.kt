package com.dongyu.movies.event

import com.dongyu.movies.model.movie.VideoSource

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
     * @param currentSourceItem 当前选择的源
     */
    fun onReloadClick(currentSourceItem: VideoSource.Item)

    /**
     * 换源
     */
    fun onSwitchSourceClick()
}