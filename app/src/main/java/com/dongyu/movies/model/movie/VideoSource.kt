package com.dongyu.movies.model.movie

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelableCompat
import com.dongyu.movies.model.parser.PlayParam

/**
 * 视频播放源（线路）
 */
data class VideoSource(
    /**
     * 线路id
     */
    val id: String,
    /**
     * 名称（某某线路）
     */
    val name: String,
    /**
     * 该源下的播放项
     */
    val items: List<Item> = mutableListOf(),
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
    ) {
       parcel.readList(items, Item::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeList(items)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoSource> {
        override fun createFromParcel(parcel: Parcel): VideoSource {
            return VideoSource(parcel)
        }

        override fun newArray(size: Int): Array<VideoSource?> {
            return arrayOfNulls(size)
        }
    }

    /**
     * 播放项
     */
    data class Item(
        /**
         * 播放项名称
         */
        val name: String,
        /**
         * 索引位置
         */
        val index: Int,
        /**
         * 播放所需参数
         */
        val param: PlayParam,
    ): Parcelable {
        /**
         * 选中状态
         */
        var selected = false

        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readInt(),
            parcel.readParcelable<PlayParam>(PlayParam::class.java.classLoader)!!
        ) {
            selected = parcel.readByte() != 0.toByte()
        }

        constructor(item: Item): this(name = item.name, index = item.index, param = PlayParam(item.param)) {
            this.selected = item.selected
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Item

            if (name != other.name) return false
            if (param != other.param) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + param.hashCode()
            return result
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeInt(index)
            parcel.writeParcelable(param, flags)
            parcel.writeByte(if (selected) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Item> {
            override fun createFromParcel(parcel: Parcel): Item {
                return Item(parcel)
            }

            override fun newArray(size: Int): Array<Item?> {
                return arrayOfNulls(size)
            }
        }
    }
}
