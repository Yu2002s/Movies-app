package com.dongyu.movies.fragment.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import com.dongyu.movies.activity.MainActivity
import com.dongyu.movies.activity.VideoActivity
import com.dongyu.movies.adapter.FilterAdapter
import com.dongyu.movies.adapter.MovieGridPagingAdapter
import com.dongyu.movies.base.BaseFragment
import com.dongyu.movies.data.home.ClassifyQueryParam
import com.dongyu.movies.data.home.FilterData
import com.dongyu.movies.data.movie.PlayParam
import com.dongyu.movies.databinding.FragmentClassifyBinding
import com.dongyu.movies.dialog.RouteDialog
import com.dongyu.movies.event.OnCardItemClickListener
import com.dongyu.movies.event.OnItemClickListener
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.viewmodel.ClassifyViewModel
import com.dongyu.movies.viewmodel.ClassifyViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ClassifyFragment : BaseFragment() {

    companion object {
        const val ID: String = "id"
        private const val TAG = "ClassifyFragment"
    }

    /**
     * 生命周期为父fragment
     */
    private val viewModel by viewModels<ClassifyViewModel> {
        ClassifyViewModelFactory(requireArguments().getInt(ID, 1))
    }

    private var _binding: FragmentClassifyBinding? = null
    private val binding get() = _binding!!

    private val moviesGridAdapter = MovieGridPagingAdapter()

    private val filterList = mutableListOf<FilterData>()

    private val filterAdapter = FilterAdapter(filterList)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterRecyclerView.adapter = filterAdapter
        binding.movieRecyclerView.adapter = moviesGridAdapter

        moviesGridAdapter.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val movieItem = moviesGridAdapter.peek(position) ?: return
                VideoActivity.play(PlayParam(viewModel.movieId, movieItem.id, movieItem.routeId))
            }
        }

        filterAdapter.onCardItemClickListener = object : OnCardItemClickListener {
            override fun onCardItemClick(
                outerView: View,
                innerView: View,
                outerPosition: Int,
                innerPosition: Int
            ) {
                if (binding.refreshLayout.isRefreshing) {
                    return "正在刷新，请稍后再操作".showToast()
                }
                val filterData = filterList[outerPosition]
                val item = filterData.items[innerPosition]
                filterData.items.forEachIndexed { index, data ->
                    if (data.isSelect) {
                        data.isSelect = false
                        filterAdapter.notifyItemChanged(outerPosition, index)
                    }
                }
                item.isSelect = true
                innerView.isSelected = true

                val param = viewModel.getParam()
                val paramClass = ClassifyQueryParam::class.java
                val fields = paramClass.declaredFields

                val field = fields.first {
                    it.name == filterData.id
                }
                field.isAccessible = true
                field.set(param, if (innerPosition == 0) null else item.id ?: item.value)
                moviesGridAdapter.refresh()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.classifyState.collectLatest {
                    moviesGridAdapter.submitData(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                moviesGridAdapter.loadStateFlow.collect {
                    val loadingFinished = it.refresh !is LoadState.Loading
                    binding.refreshLayout.isRefreshing = !loadingFinished
                    binding.loading.isInvisible = loadingFinished

                    if (it.refresh is LoadState.Error) {
                        showRouteDialog()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.filterState.collect {
                    filterList.clear()
                    filterList.addAll(it)
                    filterAdapter.notifyDataSetChanged()
                }
            }
        }

        binding.btnBackTop.setOnClickListener {
            binding.filterRecyclerView.isVisible = !binding.filterRecyclerView.isVisible
            binding.movieRecyclerView.scrollToPosition(0)
        }

        binding.refreshLayout.setOnRefreshListener {
            moviesGridAdapter.refresh()
        }
    }

    private fun showRouteDialog() {
        RouteDialog(requireActivity() as AppCompatActivity) {
            "正在刷新，请稍后".showToast()
            moviesGridAdapter.refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).showAppBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}