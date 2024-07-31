package com.dongyu.movies.data.base

data class BaseResponse<T>(
  val code: Int = 0,
  val data: T,
  val msg: String = "",
)
