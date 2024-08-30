package com.dongyu.movies.base

import android.os.Looper
import android.util.Log
import com.dongyu.movies.BuildConfig
import com.dongyu.movies.activity.LoginActivity
import com.dongyu.movies.api.AppService
import com.dongyu.movies.api.HomeService
import com.dongyu.movies.api.MovieService
import com.dongyu.movies.api.SearchService
import com.dongyu.movies.api.UserService
import com.dongyu.movies.data.base.BaseResponse
import com.dongyu.movies.data.user.User
import com.dongyu.movies.utils.AesEncryption
import com.dongyu.movies.utils.SpUtils
import com.dongyu.movies.utils.SpUtils.get
import com.dongyu.movies.utils.SpUtils.getOrDefaultNumber
import com.dongyu.movies.utils.SpUtils.put
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.utils.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

suspend fun <T> requestCallFlow(block: suspend () -> Call<BaseResponse<T>>) =
    requestFlow { block.invoke().await() }

suspend fun <T> requestFlow(block: suspend () -> BaseResponse<T>) =
    flowOf(requestResult(block)).flowOn(Dispatchers.IO)

suspend fun <T> requestResult(block: suspend () -> BaseResponse<T>) = runCatching {
    val response = block.invoke()
    if (response.code != 200) {
        throw Throwable(response.msg)
    }
    response.data
}

suspend fun <T> requestCallResult(block: suspend () -> Call<BaseResponse<T>>) =
    requestResult { block.invoke().await() }

object BaseRepository {

    private const val API_HOST_DEBUG = "http://192.168.31.138"
    private const val API_HOST_RELEASE = "http://movies.jdynb.xyz"

    private val BASE_URL = API_HOST_RELEASE
    // if (BuildConfig.BUILD_TYPE == "debug") API_HOST_DEBUG else API_HOST_RELEASE

    private const val SP_USER = "user"
    private const val KEY_USER_ID = "id"
    private const val KEY_USER_TOKEN = "token"
    private const val KEY_USER_EMAIL = "email"
    private const val KEY_USER_NICK_NAME = "nickname"
    private const val KEY_USER_AVATAR = "avatar"
    private const val API_TOKEN_KEY = "m8wZ0TSYN2"

    var user: User? = null
        private set

    var token: String? = null
        private set

    init {
        val id: Int = SP_USER.getOrDefaultNumber(KEY_USER_ID, 0)
        val email: String? = SP_USER.get(KEY_USER_EMAIL, null)
        val nickname: String? = SP_USER.get(KEY_USER_NICK_NAME, null)
        val avatar: String? = SP_USER.get(KEY_USER_AVATAR, null)
        val token: String? = SP_USER.get(KEY_USER_TOKEN, null)

        if (id != 0 && email != null && nickname != null
        ) {
            user = User(id, nickname, email, avatar)
        }
        this.token = token
    }

    fun isLogin() = token != null

    fun saveToken(token: String) {
        this.token = token
        token.put(SP_USER, KEY_USER_TOKEN)
    }

    fun saveUser(user: User) {
        this.user = user
        user.apply {
            id.put(SP_USER, KEY_USER_ID)
            nickname.put(SP_USER, KEY_USER_NICK_NAME)
            avatar?.put(SP_USER, KEY_USER_AVATAR)
            email.put(SP_USER, KEY_USER_EMAIL)
        }
    }

    fun logout() {
        user = null
        token = null
        SpUtils.clear(SP_USER)
    }

    private fun Int.handleResponse() {
        when {
            // this in 300 until 400 -> "".showToast()
            this == 401 -> {
                logout()
                // 前往登录
                startActivity<LoginActivity>()
            }

            this == 404 -> {
                Looper.prepare()
                "访问地址不存在".showToast()
                Looper.loop()
            }

            this >= 500 -> {
                Looper.prepare()
                "服务器内部错误".showToast()
                Looper.loop()
            }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            var request = chain.request()
            var str = "来呀来呀充八万"
            str += "!"
            val now = System.currentTimeMillis()
            val apiToken = AesEncryption.encrypt(API_TOKEN_KEY + now)
            val newRequest = request.newBuilder()
                .addHeader("Time", now.toString())
                .addHeader("Api-Token", apiToken)

            token?.let {
                newRequest.addHeader("Authorization", it)
            }

            request = newRequest.build()
            val response = chain.proceed(request)
            response.code().handleResponse()
            response
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun userService() = retrofit.create<UserService>()

    fun searchService() = retrofit.create<SearchService>()

    fun movieService() = retrofit.create<MovieService>()

    fun homeService() = retrofit.create<HomeService>()

    fun appService() = retrofit.create<AppService>()
}