package com.dongyu.movies.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.cat.sdk.ad.ADMParams
import com.cat.sdk.ad.ADSplashAd
import com.dongyu.movies.config.ADConfig
import com.dongyu.movies.databinding.ActivitySplashBinding

/**
 * 应用启动页面
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity: AppCompatActivity() {

    companion object {

        private val TAG = SplashActivity::class.java.simpleName

    }

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    private var canJump: Boolean = false

    private lateinit var adSplashAd: ADSplashAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)

        initAdFn()
    }

    override fun onPause() {
        super.onPause()
        canJump = false
    }

    override fun onResume() {
        super.onResume()
        if (canJump) {
            nextPage()
        }
        canJump = true
    }

    /**
     * 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        adSplashAd.destory()
    }

    private fun initAdFn() {
        val admParams = ADMParams.Builder()
            .slotId(ADConfig.SPLASH_ID)
            .layout(binding.logoAdView)
            .build()
        adSplashAd = ADSplashAd(this, admParams, object : ADSplashAd.ADSplashAdListener {
            override fun onADClose() {
                Log.i(TAG, "onAdClose")
                nextPage()
            }

            override fun onADLoadSuccess() {
                Log.i(TAG, "onADLoadSuccess")
            }

            override fun onADShow() {
                Log.i(TAG, "onADShow")
                // 广告展示后一定要把预设的开屏图片隐藏起来
                binding.placeholder.visibility = View.INVISIBLE
            }

            override fun onADLoadedFail(code: Int, msg: String) {
                Log.i(TAG, "onADLoadedFail")
                nextPage()
            }

            override fun onADClick() {
                Log.i(TAG, "onADClick")
            }
        })
        adSplashAd.loadAD()
    }

    /**
     * 设置一个变量来控制当前开屏页面是否可以跳转，当开屏广告为普链类广告时，点击会打开一个广告落地页，
     * 此时开发者还不能打开自己的App主页。当从广告落地页返回以后，
     * 才可以跳转到开发者自己的App主页；当开屏广告是App类广告时只会下载App。
     */
    private fun nextPage() {
        if (canJump) {
            this.startActivity(Intent(this, MainActivity::class.java))
            this.finish()
            overridePendingTransition(0, 0)
        } else {
            canJump = true
        }
    }
}