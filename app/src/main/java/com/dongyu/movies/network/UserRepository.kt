package com.dongyu.movies.network

import com.dongyu.movies.api.UserService
import com.dongyu.movies.base.BaseRepository
import com.dongyu.movies.base.requestCallFlow
import com.dongyu.movies.base.requestCallResult
import com.dongyu.movies.base.requestFlow
import com.dongyu.movies.base.requestResult
import com.dongyu.movies.data.base.BaseResponse
import com.dongyu.movies.data.user.LoginFrom
import retrofit2.await
import retrofit2.create

object UserRepository {

  private val userService = BaseRepository.userService()

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