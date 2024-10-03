package com.dongyu.movies.fragment.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dongyu.movies.R
import com.dongyu.movies.activity.VideoActivity
import com.dongyu.movies.base.BaseFragment
import com.dongyu.movies.model.movie.BaseMovieItem
import com.dongyu.movies.model.movie.MovieResponse
import com.dongyu.movies.databinding.FragmentSearchBinding
import com.dongyu.movies.databinding.ItemListMovieBinding
import com.dongyu.movies.databinding.ItemSelectMovieBinding
import com.dongyu.movies.model.movie.MovieItem
import com.dongyu.movies.model.parser.ParseParam
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.viewmodel.SearchViewModel
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import kotlinx.coroutines.launch

class SearchFragment : BaseFragment() {

    companion object {

        private val TAG = SearchFragment::class.simpleName

    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel by viewModels<SearchViewModel>(ownerProducer = {
        requireActivity()
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val refreshLayout = binding.refreshLayout
        refreshLayout.onRefresh {
            Log.d(TAG, "refresh: $index")
            // 这里获取数据并添加
            if (index == 1) {
                searchViewModel.refresh()
            } else {
                searchViewModel.loadMore()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.searchListFlow.collect { result ->
                Log.d(TAG, "result: $result")
                result.onSuccess { data ->
                    refreshLayout.addData(data.result) {
                        refreshLayout.index < data.lastPage
                    }
                }.onFailure {
                    Log.e(TAG, it.message.toString())
                    handleSearchError(it)
                }
            }
        }

        binding.movieRecyclerview.setup {
            addType<MovieResponse.Movie>(R.layout.item_select_movie)
            onBind {
                val movie = getModel<MovieResponse.Movie>()
                getBinding<ItemSelectMovieBinding>().root.apply {
                    isSelected = movie.selected
                    text = movie.name
                    val icon = if (isSelected) ContextCompat.getDrawable(
                        this.context,
                        R.drawable.baseline_done_24
                    ) else null
                    setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
                }
            }
            R.id.tv_select.onClick {
                val movie = getModel<MovieResponse.Movie>()
                models?.forEachIndexed { index, m ->
                    m as MovieResponse.Movie
                    if (m.selected) {
                        m.selected = false
                        notifyItemChanged(index)
                    }
                }
                movie.selected = true
                refresh(movie)
                notifyItemChanged(modelPosition)
            }
        }

        binding.resultRecyclerview.setup {
            addType<MovieItem>(R.layout.item_list_movie)

            onBind {
                val movieItem = getModel<MovieItem>()
                getBinding<ItemListMovieBinding>().apply {
                    movieName.text = movieItem.tvName
                    Glide.with(this@SearchFragment)
                        .load(movieItem.cover)
                        .placeholder(R.drawable.image_loading)
                        .override(100, 225)
                        .into(movieCover)
                    movieCover.setRadius(20)
                    status.text = movieItem.status
                    cate.text = movieItem.cate
                    movieTag.isVisible = movieItem.years.isNotEmpty() && movieItem.area.isNotEmpty()
                            && movieItem.type.isNotEmpty()
                    movieTag.text = movieItem.years + " " + movieItem.area + " " + movieItem.type
                    movieStar.text = getString(R.string.star).format(movieItem.star)
                    movieDirector.isVisible = movieItem.director.isNotEmpty()
                    movieDirector.text = getString(R.string.director).format(movieItem.director)
                }
            }
            R.id.movie_item.onClick {
                val movie = getModel<MovieItem>()
                if (requireActivity() is VideoActivity) {
                    requireActivity().finish()
                }

               val selectMovie = binding.movieRecyclerview.models?.find {
                    it as MovieResponse.Movie
                    it.selected
                } as MovieResponse.Movie

                VideoActivity.play(ParseParam(movieId = selectMovie.id, detailId = movie.id, tvName = movie.tvName))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.searchMovieState.collect {
                updateMovieList(it)
            }
        }
    }

    /**
     * 更新影视列表
     */
    private fun updateMovieList(movies: List<MovieResponse.Movie>) {
        binding.divider.isVisible = movies.isNotEmpty()
        if (movies.isNotEmpty()) {
            movies[0].selected = true
            refresh(movies[0])
        }
        binding.movieRecyclerview.models = movies
    }

    /**
     * 处理搜索错误
     */
    private fun handleSearchError(error: Throwable) {
        val refreshLayout = binding.refreshLayout
        refreshLayout.showError(error.message)
        // 当错误时，跳转到下一页
        val page = refreshLayout.index
        if (page != 1) {
            return
        }
        val movieRecyclerview = binding.movieRecyclerview
        // 加载下一个源的数据
        val models = movieRecyclerview.models ?: return
        // 获取到当前选中的位置
        val index = models.indexOfFirst {
            it as MovieResponse.Movie
            it.selected
        }
        val nextIndex = index + 1
        // 跳转到下一个位置
        if (nextIndex >= models.size) {
            "已经是最后一个线路了".showToast()
            return
        }
        // 之前选中的
        val beforeMovie = models[index] as MovieResponse.Movie
        // 取消选中
        beforeMovie.selected = false
        movieRecyclerview.bindingAdapter.notifyItemChanged(index)
        val movie = models[nextIndex] as MovieResponse.Movie
        // 执行选中并进行刷新
        movie.selected = true
        movieRecyclerview.bindingAdapter.notifyItemChanged(nextIndex)
        refresh(movie)
    }

    /**
     * 刷新指定影视下的数据
     */
    private fun refresh(movie: MovieResponse.Movie) {
        searchViewModel.refresh(movie)
        binding.refreshLayout.showLoading()
    }

    /**
     * 搜索数据
     */
    fun search(keyword: String) {
        searchViewModel.search(keyword)
        _binding?.refreshLayout?.showLoading(refresh = !searchViewModel.isFirstSearch)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}