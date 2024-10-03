package com.dongyu.movies.model.parser

import android.os.Parcel
import android.os.Parcelable

/**
 * 需要的一些参数获取实际播放地址 /detailId/routeId/selectionId
 */
data class PlayParam(
    /**
     * 详情id
     */
    val detailId: String,
    /**
     * 线路id
     */
    var sourceId: String,
    /**
     * 集数id
     */
    var selectionId: String,
): Parcelable {

    /**
     * 解析id
     */
    var parseId: Int = -1
    /**
     * 视频解析地址
     */
    var videoUrl: String = ""

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
        parseId = parcel.readInt()
        videoUrl = parcel.readString() ?: ""
    }

    constructor(param: PlayParam) : this(
        detailId = param.detailId,
        sourceId = param.sourceId,
        selectionId = param.selectionId
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayParam

        if (detailId != other.detailId) return false
        if (sourceId != other.sourceId) return false
        if (selectionId != other.selectionId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = detailId.hashCode()
        result = 31 * result + sourceId.hashCode()
        result = 31 * result + selectionId.hashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(detailId)
        parcel.writeString(sourceId)
        parcel.writeString(selectionId)
        parcel.writeInt(parseId)
        parcel.writeString(videoUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlayParam> {
        override fun createFromParcel(parcel: Parcel): PlayParam {
            return PlayParam(parcel)
        }

        override fun newArray(size: Int): Array<PlayParam?> {
            return arrayOfNulls(size)
        }
    }


}
