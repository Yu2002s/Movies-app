package com.dongyu.movies.config

/**
 * SharedPreference key配置
 */
class SPConfig {

    companion object {

        /**
         * 当前的影视id
         */
        const val CURRENT_ROUTE_ID = "route_id"

        /**
         * 自动播放下一集
         */
        const val PLAYER_AUTO_NEXT = "auto_next"

        /**
         * 长按倍速
         */
        const val PLAYER_LONG_PRESS_SPEED = "long_press_speed"

        /**
         * 自动全屏
         */
        const val PLAYER_AUTO_FULLSCREEN = "auto_fullscreen"

        /**
         * 自动切换线路
         */
        const val PLAYER_AUTO_SWITCH_ROUTE = "auto_switch_route"

        /**
         * 跳过片头
         */
        const val PLAYER_SKIP_START = "skip_start"

        /**
         * 跳过片头时长
         */
        const val PLAYER_SKIP_START_TIME = "skip_start_time"

        /**
         * 跳过片尾
         */
        const val PLAYER_SKIP_END = "skip_end"

        /**
         * 跳过片尾时长
         */
        const val PLAYER_SKIP_END_TIME = "skip_end_time"

        /**
         * 是否显示弹幕
         */
        const val PLAYER_SHOW_DANMAKU = "show_danmaku"

        /**
         * 播放显示缩放模式
         */
        const val PLAYER_SCALE_MODE = "play_scale_mode"

        /**
         * 全屏底部的小进度条
         */
        const val PLAYER_SMALL_PROGRESS = "small_progress"

        /**
         * 弹幕行数
         */
        const val PLAYER_DANMAKU_LINE = "danmaku_line"

        /**
         * 弹幕透明度
         */
        const val PLAYER_DANMAKU_ALPHA = "danmaku_alpha"

        /**
         * 弹幕大小
         */
        const val PLAYER_DANMAKU_SIZE = "danmaku_size"

        /**
         * 弹幕间距
         */
        const val PLAYER_DANMAKU_MARGIN = "danmaku_margin"

        /**
         * 弹幕滚动速度
         */
        const val PLAYER_DANMAKU_SPEED = "danmaku_speed"

        /**
         * App主题样式
         */
        const val APP_THEME = "theme"

        /**
         * 我的页面封面地址
         */
        const val MINE_COVER = "mine_cover"
    }
}