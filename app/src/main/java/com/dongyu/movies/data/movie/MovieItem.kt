package com.dongyu.movies.data.movie

import android.os.Parcel
import android.os.Parcelable

data class MovieItem(
    val type: String = "",
    val area: String = "",
    val score: Float = 0f,
    val detail: String = "",
    val years: Int = 0
): BaseMovieItem(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readFloat(),
        parcel.readString() ?: "",
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(area)
        parcel.writeFloat(score)
        parcel.writeString(detail)
        parcel.writeInt(years)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MovieItem> {
        override fun createFromParcel(parcel: Parcel): MovieItem {
            return MovieItem(parcel)
        }

        override fun newArray(size: Int): Array<MovieItem?> {
            return arrayOfNulls(size)
        }
    }
}