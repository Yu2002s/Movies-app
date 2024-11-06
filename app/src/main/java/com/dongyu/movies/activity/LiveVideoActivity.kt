package com.dongyu.movies.activity

import android.os.Bundle
import androidx.core.content.IntentCompat
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.databinding.ActivityLiveVideoBinding
import com.dongyu.movies.model.movie.LiveSourceItem
import com.dongyu.movies.utils.startActivity

class LiveVideoActivity: BaseActivity() {

    companion object {

        const val PARAM_SOURCE = "source"

        fun play(liveSourceItem: LiveSourceItem) {
            startActivity<LiveVideoActivity>(PARAM_SOURCE to liveSourceItem)
        }

    }

    private val binding by lazy {
        ActivityLiveVideoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val liveSourceItem =
            IntentCompat.getParcelableExtra(intent, PARAM_SOURCE, LiveSourceItem::class.java)

        liveSourceItem?.let {
            binding.player.play(it.url)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.player.stop()
    }
}