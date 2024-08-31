package com.dongyu.movies.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import com.dongyu.movies.activity.VideoActivity
import com.dongyu.movies.adapter.HistoryListAdapter
import com.dongyu.movies.data.history.PlayHistory
import com.dongyu.movies.data.movie.PlayParam
import com.dongyu.movies.data.search.History
import com.dongyu.movies.databinding.FragmentHistoryBinding
import com.dongyu.movies.event.OnItemClickListener
import com.dongyu.movies.utils.ioThread
import com.dongyu.movies.viewmodel.HistoryViewModel
import com.scwang.smart.refresh.header.ClassicsHeader
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.litepal.LitePal
import org.litepal.extension.delete

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val historyViewModel by viewModels<HistoryViewModel>()

    private val historyListAdapter = HistoryListAdapter()

    private var isFirst = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            root.setRefreshHeader(ClassicsHeader(requireContext()))
        }
        binding.historyRecyclerView.adapter = historyListAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                historyViewModel.historyFlow.collectLatest {
                    historyListAdapter.submitData(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            historyListAdapter.loadStateFlow.collect {
                if (it.refresh !is LoadState.Loading) {
                    // binding.root.isRefreshing = false
                    if (binding.root.isRefreshing) {
                        binding.root.finishRefresh()
                    }
                }
            }
        }

        historyListAdapter.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                historyListAdapter.peek(position)?.let {
                    VideoActivity.play(
                        PlayParam(
                            id = it.movieId,
                            detailId = it.detailId,
                            routeId = it.routeId ?: 1,
                            selection = it.selection ?: 1
                        )
                    )
                }
            }
        }

        historyListAdapter.onItemLongClickListener = object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val history = historyListAdapter.peek(position) ?: return
                ioThread {
                    LitePal.delete<PlayHistory>(history.id)
                    historyListAdapter.refresh()
                }
            }
        }

        binding.root.setOnRefreshListener {
            historyListAdapter.refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFirst) {
            isFirst = false
        } else {
            historyListAdapter.refresh()
        }
        //
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}