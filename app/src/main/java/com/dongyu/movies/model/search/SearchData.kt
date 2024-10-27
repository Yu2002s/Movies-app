package com.dongyu.movies.model.search

import com.dongyu.movies.model.movie.MovieItem
import com.dongyu.movies.model.page.PageResult

/**
 * 搜索数据
 * 可能需要验证码的情况，需要进行二次验证
 */
data class SearchData(

    /**
     * 搜索结果（分页数据）
     */
    val pageResult: PageResult<MovieItem> = PageResult(),

    /**
     * 验证数据（不为空则需要验证）
     */
    val verifyData: VerifyData? = null,
) {
    constructor(verifyData: VerifyData): this(PageResult(), verifyData)
}

/**
 * 验证数据
 * （一般验证码是通过图片进行验证，且图片需要携带cookie，发送请求对cookie用户进行授权）
 */
data class VerifyData(

    /**
     * 验证码图片地址
     */
    val codeImg: String? = null,

    /**
     * 图片字节数据
     */
    val codeBytes: ByteArray? = null,
    /**
     * 携带返回的cookie信息
     */
    // val cookie: String,
) {

    constructor(codeBytes: ByteArray?): this(null, codeBytes)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VerifyData

        if (codeImg != other.codeImg) return false
        if (codeBytes != null) {
            if (other.codeBytes == null) return false
            if (!codeBytes.contentEquals(other.codeBytes)) return false
        } else if (other.codeBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = codeImg?.hashCode() ?: 0
        result = 31 * result + (codeBytes?.contentHashCode() ?: 0)
        return result
    }
}
