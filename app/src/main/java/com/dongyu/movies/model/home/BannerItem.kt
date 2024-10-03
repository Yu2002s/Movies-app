package com.dongyu.movies.model.home

import android.os.Parcel
import android.os.Parcelable

data class BannerItem(
    val id: String = "",
    val name: String = "",
    val cover: String = "",
    val status: String = "",
    val desc: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
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
