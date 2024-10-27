package com.dongyu.movies.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.IntentCompat
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.databinding.ActivityLiveVideoBinding
import com.dongyu.movies.model.movie.LiveSource
import com.dongyu.movies.utils.startActivity

class LiveVideoActivity: BaseActivity() {

    companion object {

        const val PARAM_SOURCE = "source"

        fun play(liveSource: LiveSource) {
            startActivity<LiveVideoActivity>(PARAM_SOURCE to liveSource)
        }

    }

    private val binding by lazy {
        ActivityLiveVideoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val liveSource =
            IntentCompat.getParcelableExtra(intent, PARAM_SOURCE, LiveSource::class.java)

        liveSource?.let {
            binding.player.play(it.url)
            
            /*val map = mutableMapOf<String, String>()
                map.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
                map.put("Accept-Encoding", "identity;q=1, *;q=0")
                map.put("pragma", "no-cache")
                map.put("cache-control", "no-cache")
                map.put("sec-ch-ua-platform", "\"Windows\"")
                map.put("sec-ch-ua", "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"")
                map.put("sec-ch-ua-mobile", "?0")
                map.put("origin", "https://ddys.mov")
                map.put("sec-fetch-site", "cross-site")
                map.put("sec-fetch-mode", "cors")
                map.put("sec-fetch-dest", "video")
                map.put("referer", "https://ddys.mov/")
                map.put("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                map.put("range", "bytes=0-")
                map.put("priority", "i")
            
            binding.player.setHeaders(map)
            binding.player.play("https://v.ddys.pro/v/movie/Alien.Romulus.2024.mp4")*/
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.player.stop()
    }
}