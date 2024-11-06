package com.dongyu.movies.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dongyu.movies.databinding.FragmentLiveBinding
import com.dongyu.movies.model.movie.LiveSource
import com.dongyu.movies.network.LiveRepository
import com.dongyu.movies.utils.showToast
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class LiveFragment : Fragment() {

    private var _binding: FragmentLiveBinding? = null
    private val binding get() = _binding!!

    private val liveSources = mutableListOf<LiveSource>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val liveAdapter = LiveListAdapter()
        binding.viewpager.adapter = liveAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = liveSources[position].title
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

        lifecycleScope.launch {
            LiveRepository.getSourceList().collect {
                it.onSuccess { data ->
                    liveSources.clear()
                    liveSources.addAll(data)
                    liveAdapter.notifyDataSetChanged()
                }.onFailure { err ->
                    err.message.showToast()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class LiveListAdapter : FragmentStateAdapter(childFragmentManager, lifecycle) {

        override fun getItemCount() = liveSources.size

        override fun createFragment(position: Int) =
            LiveSourcesFragment.newInstance(liveSources[position].url)

    }

}