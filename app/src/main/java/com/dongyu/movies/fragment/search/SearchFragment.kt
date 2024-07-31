package com.dongyu.movies.fragment.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.RadioButton
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import com.dongyu.movies.activity.MainActivity
import com.dongyu.movies.activity.VideoActivity
import com.dongyu.movies.adapter.MovieListAdapter
import com.dongyu.movies.base.BaseFragment
import com.dongyu.movies.data.movie.MovieResponse
import com.dongyu.movies.data.movie.PlayParam
import com.dongyu.movies.data.search.SearchParam
import com.dongyu.movies.databinding.FragmentSearchBinding
import com.dongyu.movies.event.OnItemClickListener
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : BaseFragment() {

    companion object {

        private val TAG = SearchFragment::class.simpleName

    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel by viewModels<SearchViewModel>()

    private val movieListAdapter by lazy {
        MovieListAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resultRecyclerview.apply {
            adapter = movieListAdapter
        }

        movieListAdapter.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val movie = movieListAdapter.peek(position) ?: return
                val movieId = binding.movieRadioGroup.checkedRadioButtonId / 123

                if (requireActivity() is VideoActivity) {
                    requireActivity().finish()
                }

                VideoActivity.play(PlayParam(movieId, movie.id, movie.routeId))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.searchMovieState.collect {
                updateMovieList(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.searchListFlow.collectLatest {
                    movieListAdapter.submitData(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                movieListAdapter.loadStateFlow.collect {
                    if (it.refresh !is LoadState.Loading) {
                        binding.root.isRefreshing = false
                    }
                    val top = binding.topProgressBar
                    val bottom = binding.bottomProgressBar
                    updateLoadState(top, it.refresh)
                    updateLoadState(bottom, it.append)
                }
            }
        }

        binding.root.setOnRefreshListener {
            movieListAdapter.refresh()
            lifecycleScope.launch {
                searchViewModel.getMovieList()
            }
        }
    }

    private fun updateLoadState(view: View, loadState: LoadState) {
        when (loadState) {
            is LoadState.NotLoading -> {
                view.isInvisible = true
            }
            is LoadState.Loading -> {
                view.isInvisible = false
            }
            is LoadState.Error -> {
                view.isInvisible = true
                loadState.error.message.showToast()
            }
        }
    }

    private fun updateMovieList(movies: List<MovieResponse.Movie>) {
        val radioGroup = binding.movieRadioGroup
        if (radioGroup.childCount > 0) {
            radioGroup.removeAllViews()
        }
        val param = MarginLayoutParams(-1, -1)
        param.bottomMargin = 30
        movies.forEachIndexed { index, movie ->
            val radioButton = RadioButton(requireContext())
            radioButton.layoutParams = param
            radioButton.id = movie.id * 123
            radioButton.text = movie.name
            radioButton.isChecked = index == 0
            radioGroup.addView(radioButton)
        }

        binding.movieRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val movieId = checkedId / 123
            refresh(movieId)
        }
    }

    private fun refresh(movieId: Int) {
        searchViewModel.refresh(movieId)
        movieListAdapter.refresh()
    }

    /**
     * 搜索数据
     */
    fun search(keyword: String) {
        val buttonId = binding.movieRadioGroup.checkedRadioButtonId
        val movieId = if (buttonId == -1) 1 else buttonId / 123
        searchViewModel.search(SearchParam(keyword, movieId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}