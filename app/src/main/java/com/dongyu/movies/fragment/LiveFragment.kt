package com.dongyu.movies.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dongyu.movies.databinding.FragmentLiveBinding
import com.dongyu.movies.model.movie.LiveItem
import com.dongyu.movies.network.LiveRepository
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class LiveFragment : Fragment() {

    private var _binding: FragmentLiveBinding? = null
    private val binding get() = _binding!!

    private val liveItems = mutableListOf(
        LiveItem("默认", "https://cdn.jsdelivr.net/gh/BurningC4/Chinese-IPTV@master/TV-IPV4.m3u"),
        LiveItem("默认2", "https://ghp.ci/raw.githubusercontent.com/joevess/IPTV/main/home.m3u8"),
        LiveItem("默认3", "https://ghp.ci/raw.githubusercontent.com/joevess/IPTV/main/sources/iptv_sources.m3u8"),
        LiveItem("默认(IPV6)", LiveRepository.LIVE_M3U_HOST),
        LiveItem("IPTV", "https://iptv-org.github.io/iptv/index.m3u")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewpager.adapter = LiveListAdapter()

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = liveItems[position].title
        }.attach()

        val searchItem = binding.toolbar.menu.getItem(0)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                val sourcesFragment =
                    childFragmentManager.fragments[binding.viewpager.currentItem] as LiveSourcesFragment
                sourcesFragment.search(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class LiveListAdapter : FragmentStateAdapter(childFragmentManager, lifecycle) {

        override fun getItemCount() = liveItems.size

        override fun createFragment(position: Int) =
            LiveSourcesFragment.newInstance(liveItems[position].source)

    }

}