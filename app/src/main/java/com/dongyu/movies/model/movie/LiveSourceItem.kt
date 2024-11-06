package com.dongyu.movies.model.movie

import android.os.Parcel
import android.os.Parcelable

data class LiveSourceItem(
    val id: String,
    val title: String,
    val groupTitle: String,
    val logo: String,
    val url: String
) : Parcelable {
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
        parcel.writeString(title)
        parcel.writeString(groupTitle)
        parcel.writeString(logo)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LiveSourceItem> {
        override fun createFromParcel(parcel: Parcel): LiveSourceItem {
            return LiveSourceItem(parcel)
        }

        override fun newArray(size: Int): Array<LiveSourceItem?> {
            return arrayOfNulls(size)
        }
    }

}

data class LiveSource(
    val title: String,
    val url: String
)