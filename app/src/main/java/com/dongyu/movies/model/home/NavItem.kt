package com.dongyu.movies.model.home

import kotlin.random.Random

class NavItem(
    val id: String = HOME,
    val title: String
) {

    var type: Int = TYPE_NORMAL

    /**
     * 唯一标识，用于刷新
     */
    private val num = Random(100).nextInt(1000)

    companion object {

        /**
         * 主页id
         */
        const val HOME = "-1"

        /**
         * 普通普通影视
         */
        const val TYPE_NORMAL = 0

        /**
         * 短剧
         */
        const val TYPE_SHORT = 1
    }

    override fun hashCode(): Int {
        var result = id.hashCode() + num
        result = 31 * result + title.hashCode()
        result = 31 * result + type
        return result
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}
