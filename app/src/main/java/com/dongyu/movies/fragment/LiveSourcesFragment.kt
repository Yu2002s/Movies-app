package com.dongyu.movies.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.dongyu.movies.R
import com.dongyu.movies.activity.LiveVideoActivity
import com.dongyu.movies.base.BaseFragment
import com.dongyu.movies.databinding.FragmentLiveSourcesBinding
import com.dongyu.movies.databinding.ItemGridLiveSourceBinding
import com.dongyu.movies.databinding.ItemListLiveBinding
import com.dongyu.movies.model.movie.LiveSource
import com.dongyu.movies.network.LiveRepository
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import kotlinx.coroutines.launch

class LiveSourcesFragment : BaseFragment() {

    private var _binding: FragmentLiveSourcesBinding? = null
    private val binding get() = _binding!!

    private val sources = mutableListOf<LiveSource>()

    private val mHandler = Handler(Looper.getMainLooper())

    private var keyword: String = ""

    companion object {

        private const val PARAM_URL = "url"

        fun newInstance(url: String): LiveSourcesFragment {
            val args = Bundle()
            args.putString(PARAM_URL, url)
            val fragment = LiveSourcesFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveSourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rv.setup {
            addType<LiveSource>(R.layout.item_grid_live_source)
            onBind {
                val model = getModel<LiveSource>()
                getBinding<ItemGridLiveSourceBinding>().apply {
                    name.text = model.title
                    if (model.logo.isEmpty()) {
                        logo.setImageResource(R.drawable.baseline_live_tv_24)
                    } else {
                        Glide.with(this@LiveSourcesFragment)
                            .load(model.logo)
                            .placeholder(R.drawable.baseline_live_tv_24)
                            .override(Target.SIZE_ORIGINAL)
                            .into(logo)
                    }
                }
            }
            R.id.item.onClick {
                LiveVideoActivity.play(getModel())
            }
        }

        binding.root.onRefresh {
            lifecycleScope.launch {
                LiveRepository.getSourceList(requireArguments().getString(PARAM_URL)!!)
                    .collect { result ->
                        result.onSuccess {
                            sources.clear()
                            sources.addAll(it)
                            addData(it) { false }
                            Log.d("jdy", it.toString())
                        }.onFailure {
                            Log.e("jdy", it.toString())
                            showError()
                        }
                    }
            }
        }.showLoading()
    }

    private val searchRunnable = Runnable {
        if (keyword.isBlank()) {
            binding.rv.bindingAdapter.setDifferModels(sources)
            return@Runnable
        }
        binding.rv.bindingAdapter.setDifferModels(sources.filter {
            it.title.lowercase().contains(keyword.lowercase())
        })
    }

    fun search(keyword: String) {
        this.keyword = keyword
        mHandler.removeCallbacks(searchRunnable)
        mHandler.postDelayed(searchRunnable, 400)
    }

    override fun isFirstResume() {
        super.isFirstResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }
}