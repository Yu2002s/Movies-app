package com.wanban.screencast

object Utils {

    /**
     * long值转换时分秒
     */
    fun getHMSTime(time: Long): String {
        val timeStr: String?
        val hour: Long?
        var minute: Long?
        val second: Long?
        if (time <= 1000)
            return "0:00:00"
        else {
            val secondTime = time / 1000
            minute = secondTime / 60
            if (minute < 60) {
                second = secondTime % 60
                timeStr = "0:" + unitFormat(minute) + ":" + unitFormat(second)
            } else {
                hour = minute / 60
                if (hour > 99)
                    return "0:59:59"
                minute %= 60
                second = secondTime - (hour * 3600) - (minute * 60)
                timeStr = hour.toString() + ":" + unitFormat(minute) + ":" + unitFormat(second)
            }
        }
        return timeStr
    }

    private fun unitFormat(i: Long): String {
        return if (i in 0..9)
            "0$i"
        else
            i.toString()
    }

}