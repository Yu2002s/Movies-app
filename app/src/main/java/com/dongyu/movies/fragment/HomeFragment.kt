package com.dongyu.movies.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dongyu.movies.base.BaseFragment
import com.dongyu.movies.databinding.FragmentHomeBinding
import com.dongyu.movies.fragment.home.ClassifyFragment
import com.dongyu.movies.fragment.home.MainFragment
import com.dongyu.movies.viewmodel.HomeViewModel
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : BaseFragment() {

    private val viewModel by viewModels<HomeViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val fragments = mutableListOf<Class<*>>()
    private val titles = arrayOf("首页", "电影", "电视剧", "综艺", "动漫")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initFragment()
        initViews()
    }

    private fun initFragment() {
        fragments.add(MainFragment::class.java)
        repeat(titles.size - 1) {
            fragments.add(ClassifyFragment::class.java)
        }
    }

    private fun initViews() {
        val pager = binding.homeViewPager
        pager.isUserInputEnabled = false
        pager.offscreenPageLimit = 5
        pager.adapter = HomePagerAdapter(childFragmentManager, lifecycle, fragments)

        TabLayoutMediator(
            binding.homeTabLayout,
            pager,
            false,
            false
        ) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private class HomePagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        private val fragments: List<Class<*>>
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount() = fragments.size

        override fun createFragment(position: Int): Fragment = (fragments[position]
            .getConstructor().newInstance() as Fragment).apply {
            arguments = bundleOf(ClassifyFragment.ID to position)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}