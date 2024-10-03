package com.dongyu.movies.api

import com.dongyu.movies.model.base.BaseResponse
import com.dongyu.movies.model.user.User
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {

  @POST("/users")
  @FormUrlEncoded
  fun login(
    @Field("email") email: String,
    @Field("code") code: String
  ): Call<BaseResponse<String>>

  @POST("/users/code")
  @FormUrlEncoded
  fun sendCode(@Field("email") email: String): Call<BaseResponse<Nothing>>

  /**
   * 获取已登录用户的信息
   */
  @GET("/users")
  fun getLoggedInUser(): Call<BaseResponse<User>>

  @GET("/users/{id}")
  fun getUser(@Path("id") userId: Int): Call<BaseResponse<User>>
}