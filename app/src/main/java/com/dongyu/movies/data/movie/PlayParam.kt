package com.dongyu.movies.data.movie

import android.os.Parcel
import android.os.Parcelable

class PlayParam(
    val id: Int,
    val detailId: String,
    var routeId: Int = 1,
    var selection: Int = 1
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(detailId)
        parcel.writeInt(routeId)
        parcel.writeInt(selection)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "PlayParam(id=$id, detailId='$detailId', routeId=$routeId, selection=$selection)"
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