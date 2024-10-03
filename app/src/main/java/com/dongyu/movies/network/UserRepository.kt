package com.dongyu.movies.network

import com.dongyu.movies.model.user.LoginFrom

object UserRepository {

  private val userService = Repository.userService

  suspend fun login(loginFrom: LoginFrom) = requestCallFlow {
    userService.login(loginFrom.email, loginFrom.code)
  }

  /**
   * 获取用户信息
   */
  suspend fun getLoggedInUser() = requestCallFlow { userService.getLoggedInUser() }

  /**
   * 发送验证码
   */
  suspend fun sendCode(email: String) = requestCallResult { userService.sendCode(email) }

}