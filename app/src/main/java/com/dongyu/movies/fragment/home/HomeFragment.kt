package com.dongyu.movies.fragment.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dongyu.movies.R
import com.dongyu.movies.activity.SearchActivity
import com.dongyu.movies.base.BaseFragment
import com.dongyu.movies.databinding.FragmentHomeBinding
import com.dongyu.movies.dialog.MovieSourceDialog
import com.dongyu.movies.model.home.NavItem
import com.dongyu.movies.viewmodel.HomeViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {

    private val viewModel by viewModels<HomeViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val navItems = mutableListOf<NavItem>()

    private val homePagerAdapter by lazy {
        HomePagerAdapter(childFragmentManager, lifecycle)
    }

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

        initViews()
        initEvent()
        collectData()
    }

    fun updateNav(navItems: List<NavItem>) {
        viewModel.updateNav(navItems)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun collectData() {
        lifecycleScope.launch {
            viewModel.navStateFlow.collect { data ->
                navItems.clear()
                navItems.addAll(data)
                homePagerAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initViews() {
        val pager = binding.homeViewPager
        pager.isUserInputEnabled = false
        pager.offscreenPageLimit = 3
        pager.adapter = homePagerAdapter

        TabLayoutMediator(
            binding.homeTabLayout,
            pager,
            true,
            true
        ) { tab, position ->
            tab.text = navItems[position].title
        }.attach()
    }

    private fun initEvent() {
        binding.fabSwitchSource.setOnClickListener {
            if (childFragmentManager.fragments.isEmpty()) {
                return@setOnClickListener
            }
            val fragment = childFragmentManager.fragments[0]
            if (fragment is MainFragment) {
                fragment.showRouteDialog()
            }
        }

        binding.mainSearchBar.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
            requireActivity().overridePendingTransition(0, 0)
        }

        binding.mainSearchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.route -> {
                    showMovieSourceDialog()
                }
            }
            true
        }
    }

    /**
     * 显示影视源
     */
    private fun showMovieSourceDialog() {
        MovieSourceDialog(requireActivity() as AppCompatActivity) {
            for (fragment in childFragmentManager.fragments) {
                if (fragment is MainFragment) {
                    fragment.refresh()
                }
            }
        }
    }

    private inner class HomePagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount() = navItems.size

        override fun createFragment(position: Int): Fragment {
            val navItem = navItems[position]
            if (navItem.id == NavItem.HOME) {
                return MainFragment()
            }
            return ClassifyFragment().apply {
                arguments = bundleOf(ClassifyFragment.ID to navItem.id)
            }
        }

        override fun getItemId(position: Int): Long {
            val itemId = (navItems[position]).hashCode().toLong()
            return itemId
        }

        override fun containsItem(itemId: Long): Boolean {
            return navItems.map { it.hashCode().toLong() }.contains(itemId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}