package com.dongyu.movies.data.user

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class User(
    val id: Int,
    val nickname: String,
    val email: String,
    val avatar: String?,
) {
    override fun toString(): String {
        return "User(id=$id, nickname='$nickname', email='$email', avatar=$avatar)"
    }

    /*val lastIp: String = ""
    val createAt: Date? = null
    val updateAt: Date? = null*/
}
