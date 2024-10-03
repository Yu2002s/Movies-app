package com.dongyu.movies

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.dongyu.movies.utils.SSLIgnore
import okhttp3.OkHttpClient
import java.io.InputStream
import java.net.Proxy

@GlideModule
class MovieAppGlideModule : AppGlideModule() {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            var request = it.request()
            request = request.newBuilder()
                .addHeader("Referer", request.url().toString())
                .build()
            it.proceed(request)
        }
        .proxy(Proxy.NO_PROXY)
        .sslSocketFactory(SSLIgnore.getSSLSocketFactory(), SSLIgnore.getX509TrustManager())
        .hostnameVerifier(SSLIgnore.getHostnameVerifier())
        .build()

    override fun applyOptions(context: Context, builder: GlideBuilder) {
       // builder.setLogLevel(Log.ERROR)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(okHttpClient)
        )
    }
}