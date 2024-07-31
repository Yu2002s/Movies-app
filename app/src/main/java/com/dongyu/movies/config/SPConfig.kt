package com.dongyu.movies.config

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
    }
}