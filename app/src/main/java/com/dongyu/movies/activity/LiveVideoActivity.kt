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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.player.stop()
    }
}