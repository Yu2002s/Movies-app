package com.dongyu.movies.data.movie

import android.os.Parcel
import android.os.Parcelable

data class PlaySource(
    val routeId: Int,
    val name: String,
    val data: List<Item>
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.createTypedArrayList(Item) ?: emptyList()
    ) {
    }

    data class Item(
        val name: String,
        val selection: Int,
    ): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readInt()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeInt(selection)
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(routeId)
        parcel.writeString(name)
        parcel.writeTypedList(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaySource> {
        override fun createFromParcel(parcel: Parcel): PlaySource {
            return PlaySource(parcel)
        }

        override fun newArray(size: Int): Array<PlaySource?> {
            return arrayOfNulls(size)
        }
    }
}
