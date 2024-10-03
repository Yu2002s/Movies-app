package com.dongyu.movies.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.config.AppConfig
import com.dongyu.movies.databinding.ActivityCrashBinding

/**
 * App全局闪退处理
 */
class CrashActivity: BaseActivity() {

    private val binding by lazy {
        ActivityCrashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.header.toolBar)
        supportActionBar?.title = "抱歉，软件闪退了！"

        intent?.getStringExtra("log")?.let { log ->
            binding.tvCrashContent.text = log
        }

        binding.restartApp.setOnClickListener {
            // exitProcess(0)
            finish()
            // startActivity(Intent(this, MainActivity::class.java))
        }

        binding.feedback.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.QQ_GROUP_URL)))
        }
    }
}