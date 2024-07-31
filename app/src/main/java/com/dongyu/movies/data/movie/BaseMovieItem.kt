package com.dongyu.movies.data.movie

import android.os.Parcel
import android.os.Parcelable

open class BaseMovieItem(
    val id: String = "",
    val routeId: Int = 1,
    val tvName: String = "",
    val cover: String = "",
    val star: String = "",
    val director: String = "",
    val cate: String = "",
    val status: String = ""
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
    }

    override fun toString(): String {
        return "BaseMovieItem(id='$id', tvName='$tvName', cover='$cover', star='$star', director='$director', cate='$cate', status='$status')"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(routeId)
        parcel.writeString(tvName)
        parcel.writeString(cover)
        parcel.writeString(star)
        parcel.writeString(director)
        parcel.writeString(cate)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseMovieItem) return false

        if (id != other.id) return false
        if (routeId != other.routeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + routeId
        return result
    }

    companion object CREATOR : Parcelable.Creator<BaseMovieItem> {
        override fun createFromParcel(parcel: Parcel): BaseMovieItem {
            return BaseMovieItem(parcel)
        }

        override fun newArray(size: Int): Array<BaseMovieItem?> {
            return arrayOfNulls(size)
        }
    }


}
