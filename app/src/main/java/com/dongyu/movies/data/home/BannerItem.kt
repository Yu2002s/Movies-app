package com.dongyu.movies.data.home

import android.os.Parcel
import android.os.Parcelable

data class BannerItem(
    val id: String,
    val routeId: Int,
    val name: String,
    val cover: String,
    val status: String,
    val desc: String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(routeId)
        parcel.writeString(name)
        parcel.writeString(cover)
        parcel.writeString(status)
        parcel.writeString(desc)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BannerItem> {
        override fun createFromParcel(parcel: Parcel): BannerItem {
            return BannerItem(parcel)
        }

        override fun newArray(size: Int): Array<BannerItem?> {
            return arrayOfNulls(size)
        }
    }
}
