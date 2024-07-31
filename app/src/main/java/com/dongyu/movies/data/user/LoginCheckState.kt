package com.dongyu.movies.data.user

class LoginCheckState(
  val validEmail: Int? = null,
  val validCode: Int? = null,
  val isDataValid: Boolean = false
)