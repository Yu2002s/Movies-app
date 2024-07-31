package com.dongyu.movies.fragment.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dongyu.movies.R
import com.dongyu.movies.activity.VideoActivity
import com.dongyu.movies.adapter.CardAdapter
import com.dongyu.movies.adapter.RankAdapter
import com.dongyu.movies.base.BaseFragment
import com.dongyu.movies.data.home.BannerItem
import com.dongyu.movies.data.home.MainData
import com.dongyu.movies.data.home.MoviesCard
import com.dongyu.movies.data.home.RankItem
import com.dongyu.movies.data.movie.BaseMovieItem
import com.dongyu.movies.data.movie.MovieItem
import com.dongyu.movies.data.movie.PlayParam
import com.dongyu.movies.databinding.FragmentMainBinding
import com.dongyu.movies.databinding.ItemListBannerBinding
import com.dongyu.movies.dialog.RouteDialog
import com.dongyu.movies.event.OnCardItemClickListener
import com.dongyu.movies.event.OnItemClickListener
import com.dongyu.movies.utils.dp2px
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainFragment : BaseFragment() {

    companion object {

        private val TAG = MainFragment::class.java.simpleName

    }

    private val mainViewModel by viewModels<MainViewModel>(ownerProducer = {
        requireParentFragment()
    })

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val bannerList = mutableListOf<BannerItem>()
    private val cardList = mutableListOf<MoviesCard<BaseMovieItem>>()
    private val rankList = mutableListOf<RankItem>()

    private val bannerAdapter2 by lazy {
        BannerAdapter2(childFragmentManager, viewLifecycleOwner.lifecycle, bannerList)
    }

    private val cardAdapter = CardAdapter(cardList)

    private val rankAdapter = RankAdapter(rankList)

    private var isDragging = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.stateFlow.collect { result ->
                result ?: return@collect
                binding.refreshLayout.isRefreshing = false
                binding.loading.isInvisible = true
                binding.errorText.isInvisible = true
                result.onSuccess { data ->
                    bannerList.clear()
                    bannerList.addAll(data.bannerList)
                    updateBannerView()
                    updateCardList(data)
                }.onFailure { error ->
                    Log.e(TAG, error.message.toString())
                    showErrorText(error.message)
                    showRouteDialog()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.bannerState.collect {
                if (bannerList.isEmpty() || isDragging) {
                    return@collect
                }
                binding.banner.currentItem = it
            }
        }

        binding.refreshLayout.setOnRefreshListener {
            mainViewModel.refresh()
        }

        binding.errorText.setOnClickListener {
            it.isInvisible = true
            binding.loading.isInvisible = false
            mainViewModel.refresh()
        }

        binding.banner.offscreenPageLimit = 6
        binding.banner.adapter = bannerAdapter2
        /*binding.banner.pageMargin = 10.dp2px()*/

        val pool = RecyclerView.RecycledViewPool()
        pool.setMaxRecycledViews(0, 10)
        binding.cardRecyclerView.setRecycledViewPool(pool)

        binding.cardRecyclerView.adapter = cardAdapter
        // binding.rankRecyclerView.adapter = rankAdapter

        binding.banner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                isDragging = state == ViewPager.SCROLL_STATE_DRAGGING
            }
        })

        bannerAdapter2.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val bannerItem = bannerList[position]
                mainViewModel.getMainData()?.let {
                    VideoActivity.play(PlayParam(it.movieId, bannerItem.id, bannerItem.routeId))
                }
            }
        }

        cardAdapter.onCardItemClickListener = object : OnCardItemClickListener {
            override fun onCardItemClick(
                outerView: View,
                innerView: View,
                outerPosition: Int,
                innerPosition: Int
            ) {
                val routeId = mainViewModel.getCurrentRouteId() ?: return
                val movieItem = cardList[outerPosition].list[innerPosition]
                val playParam = PlayParam(routeId, movieItem.id, movieItem.routeId)
                VideoActivity.play(playParam)
            }
        }

        rankAdapter.onCardItemClickListener = object : OnCardItemClickListener {
            override fun onCardItemClick(
                outerView: View,
                innerView: View,
                outerPosition: Int,
                innerPosition: Int
            ) {
                val routeId = mainViewModel.getCurrentRouteId() ?: return
                val rankListItem = rankList[outerPosition].rankListItems[innerPosition]
                val playParam = PlayParam(routeId, rankListItem.id, rankListItem.routeId)
                VideoActivity.play(playParam)
            }
        }
    }

    fun showRankDialog() {
        val rv = RecyclerView(requireContext())
        rv.layoutParams = ViewGroup.LayoutParams(-1, -2)
        rv.layoutManager = LinearLayoutManager(requireContext()).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
        rv.adapter = rankAdapter

        BottomSheetDialog(requireContext()).apply {
            setContentView(rv)
            show()
        }
    }

    private fun showRouteDialog() {
        RouteDialog(requireActivity() as AppCompatActivity) {
            "正在刷新，请稍后".showToast()
            binding.errorText.isInvisible = true
            binding.loading.isInvisible = false
            mainViewModel.refresh()
        }
    }

    private fun showErrorText(error: String?) {
        binding.errorText.apply {
            isInvisible = false
            text = getString(R.string.error_text)
                .format(error)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateCardList(data: MainData) {
        cardList.clear()
        cardList.addAll(data.tvList)
        rankList.clear()
        rankList.addAll(data.rankList)

        cardAdapter.notifyDataSetChanged()
        // rankAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        isDragging = false
    }

    override fun onStop() {
        super.onStop()
        isDragging = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateBannerView() {
        binding.banner.apply {
            bannerAdapter2.notifyDataSetChanged()
            currentItem = mainViewModel.getCurrentItem()
        }
    }

    private class BannerAdapter2(
        fm: FragmentManager,
        lifecycle: Lifecycle,
        private val bannerList: List<BannerItem>
    ) :
        FragmentStateAdapter(fm, lifecycle) {

        var onItemClickListener: OnItemClickListener? = null

        override fun getItemCount() = if (bannerList.isEmpty()) 0 else 10000

        override fun createFragment(position: Int): Fragment {
            return BannerFragment().apply {
                arguments = bundleOf("bannerItem" to bannerList[position % bannerList.size])
                onClickListener = OnClickListener {
                    onItemClickListener?.onItemClick(it, position % bannerList.size)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bannerList.clear()
    }
}