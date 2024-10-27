package com.dongyu.movies.model.parser

import android.os.Parcel
import android.os.Parcelable

/**
 * 解析所需的参数
 */
data class ParseParam(
    /**
     * 影视id
     */
    var movieId: Int = -1,
    /**
     * 详情id
     */
    var detailId: String,
    /**
     * 解析地址
     */
    var parseUrl: String? = null,
    /**
     * 解析id，指定解析id
     */
    var parseId: Int = -1,
    /**
     * 电视名称
     */
    val tvName: String = "",
    /**
     * 源id（通常情况下不用设置此值）
     */
    var sourceId: String = "",
    /**
     * 集数id（通常情况下只有在详情页和播放页相同时才会有值，一般不需要手动赋值）
     */
    var selectionId: String = "",
): Parcelable {

    constructor(detailId: String, parseId: Int, parseUrl: String): this(detailId = detailId) {
        this.parseUrl = parseUrl
        this.parseId = parseId
    }

    constructor(movieId: Int, detailId: String) :this(detailId = detailId) {
        this.movieId = movieId
        this.detailId = detailId
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readInt(),
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(movieId)
        parcel.writeString(detailId)
        parcel.writeString(parseUrl)
        parcel.writeInt(parseId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParseParam> {
        override fun createFromParcel(parcel: Parcel): ParseParam {
            return ParseParam(parcel)
        }

        override fun newArray(size: Int): Array<ParseParam?> {
            return arrayOfNulls(size)
        }
    }
}