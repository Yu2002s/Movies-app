package com.dongyu.movies.activity

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.core.content.ContextCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dongyu.movies.R
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.databinding.ActivityHistoryCollectBinding
import com.dongyu.movies.fragment.HistoryCollectFragment
import com.google.android.material.tabs.TabLayoutMediator

class HistoryCollectActivity : BaseActivity() {

    private val binding by lazy {
        ActivityHistoryCollectBinding.inflate(layoutInflater)
    }

    var toggleMode: Boolean = false
        set(value) {
            field = value
            invalidateOptionsMenu()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.viewpager.adapter = HistoryCollectAdapter()

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = if (position == 0) "历史" else "收藏"
        }.attach()

        binding.toolbar.setOnMenuItemClickListener {
            currentFragment.onMenuItemClick(it)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val editMenuItem = menu.getItem(0)
        val resId =
            if (toggleMode) R.drawable.baseline_close_24 else R.drawable.baseline_mode_edit_24
        editMenuItem.icon = ContextCompat.getDrawable(this, resId)

        menu.getItem(1).isVisible = toggleMode
        menu.getItem(2).isVisible = toggleMode
        menu.getItem(3).isVisible = toggleMode

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private val currentFragment
        get() = supportFragmentManager.fragments[binding.viewpager.currentItem] as HistoryCollectFragment

    private inner class HistoryCollectAdapter :
        FragmentStateAdapter(supportFragmentManager, lifecycle) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int) =
            HistoryCollectFragment.newInstance(
                if (position == 0)
                    HistoryCollectFragment.TYPE_HISTORY else HistoryCollectFragment.TYPE_COLLECT
            )
    }
}