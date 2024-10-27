package com.dongyu.movies.fragment.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.R
import com.dongyu.movies.activity.VideoActivity
import com.dongyu.movies.adapter.HomeBannerAdapter
import com.dongyu.movies.adapter.RankAdapter
import com.dongyu.movies.base.BaseFragment
import com.dongyu.movies.databinding.FragmentMainBinding
import com.dongyu.movies.databinding.ItemHomeBannerBinding
import com.dongyu.movies.databinding.ItemListCardBinding
import com.dongyu.movies.dialog.MovieSourceDialog
import com.dongyu.movies.event.OnCardItemClickListener
import com.dongyu.movies.model.home.BannerItem
import com.dongyu.movies.model.home.HomeModel
import com.dongyu.movies.model.home.RankItem
import com.dongyu.movies.model.movie.loadListCardMovies
import com.dongyu.movies.model.parser.ParseParam
import com.dongyu.movies.network.Repository
import com.dongyu.movies.utils.loadImg
import com.dongyu.movies.viewmodel.MainViewModel
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.youth.banner.indicator.CircleIndicator
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

    private val rankList = mutableListOf<RankItem>()

    private val rankAdapter = RankAdapter(rankList)

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

        initView()

        rankAdapter.onCardItemClickListener = object : OnCardItemClickListener {
            override fun onCardItemClick(
                outerView: View,
                innerView: View,
                outerPosition: Int,
                innerPosition: Int
            ) {
                val routeId = Repository.currentMovieId ?: return
                val rankListItem = rankList[outerPosition].rankListItems[innerPosition]
                /*val playParam = PlayParam(routeId, rankListItem.id, rankListItem.routeId)
                VideoActivity.play(playParam)*/
            }
        }
    }

    private fun initView() {
        binding.rv.loadImg().setup {

            /**
             * 顶部banner
             */
            addType<HomeModel.Banner>(R.layout.item_home_banner)
            /**
             * 影视宫格列表
             */
            addType<HomeModel.MoviesGrid>(R.layout.item_list_card)

            onCreate {
                when (itemViewType) {
                    R.layout.item_home_banner -> {
                        getBinding<ItemHomeBannerBinding>().loadBanner()
                    }

                    R.layout.item_list_card -> {
                        getBinding<ItemListCardBinding>().cardRecyclerView.also {
                            it.isNestedScrollingEnabled = false
                            it.setHasFixedSize(
                                true
                            )
                        }.loadListCardMovies {
                            val routeId = Repository.currentMovieId ?: return@loadListCardMovies
                            VideoActivity.play(ParseParam(movieId = routeId, detailId = it.id, tvName = it.tvName))
                        }
                    }
                }
            }

            onBind {
                when (val model = getModel<HomeModel>()) {
                    is HomeModel.Banner -> {
                        getBinding<ItemHomeBannerBinding>().banner.setDatas(model.bannerList)
                    }

                    is HomeModel.MoviesGrid -> {
                        getBinding<ItemListCardBinding>().apply {
                            cardRecyclerView.models = model.cardItem.list
                            cardTitle.text = model.cardItem.title
                        }
                    }
                }
            }
        }

        val refreshLayout = binding.refreshLayout
        refreshLayout.setEnableLoadMore(false)

        lifecycleScope.launch {
            mainViewModel.stateFlow.collect { result ->
                result?.onSuccess { data ->
                    getHomeFragment().updateNav(data.navList)
                    val models = mutableListOf<HomeModel>()
                    if (data.bannerList.isNotEmpty()) {
                        models.add(HomeModel.Banner(data.bannerList))
                    }
                    data.tvList.forEach {
                        models.add(HomeModel.MoviesGrid(it))
                    }
                    refreshLayout.setNoMoreData(true)
                    refreshLayout.addData(models)
                }?.onFailure {
                    Log.e(TAG, it.toString())
                    refreshLayout.showError(it.message)
                }
            }
        }

        refreshLayout.onRefresh {
            mainViewModel.refresh()
        }.onError {
            showRouteDialog()
        }.showLoading()
    }

    private fun getHomeFragment() = requireParentFragment() as HomeFragment

    fun refresh() {
        binding.refreshLayout.showLoading()
    }

    private fun ItemHomeBannerBinding.loadBanner() {
        banner.setAdapter(HomeBannerAdapter())
            .setIndicator(CircleIndicator(requireContext()))
            .setBannerGalleryEffect(30, 8)
            .setOnBannerListener { data, _ ->
                data as BannerItem
                if (data.id.isEmpty()) return@setOnBannerListener
                val currentMovieId = Repository.currentMovieId ?: return@setOnBannerListener
                VideoActivity.play(ParseParam(movieId = currentMovieId, detailId = data.id, tvName = data.name))
            }
            .addBannerLifecycleObserver(viewLifecycleOwner)
            .setIntercept(false)
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

    fun showRouteDialog() {
        MovieSourceDialog(requireActivity() as AppCompatActivity) {
            binding.refreshLayout.showLoading()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}