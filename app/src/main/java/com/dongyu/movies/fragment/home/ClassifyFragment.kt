package com.dongyu.movies.fragment.home

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.R
import com.dongyu.movies.activity.VideoActivity
import com.dongyu.movies.base.BaseFragment
import com.dongyu.movies.databinding.FragmentClassifyBinding
import com.dongyu.movies.databinding.ItemFilterBinding
import com.dongyu.movies.databinding.ItemListFilterBinding
import com.dongyu.movies.dialog.MovieSourceDialog
import com.dongyu.movies.model.home.CategoryData
import com.dongyu.movies.model.home.ClassifyQueryParam
import com.dongyu.movies.model.home.FilterData
import com.dongyu.movies.model.movie.loadListCardMovies
import com.dongyu.movies.model.parser.ParseParam
import com.dongyu.movies.network.MovieRepository
import com.dongyu.movies.network.Repository
import com.dongyu.movies.utils.loadImg
import com.dongyu.movies.utils.showToast
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import kotlinx.coroutines.launch

class ClassifyFragment : BaseFragment() {

    companion object {
        const val ID: String = "id"
        private const val TAG = "ClassifyFragment"
    }

    private val classifyQueryParam by lazy {
        ClassifyQueryParam(cateId = requireArguments().getString(ID, "1"))
    }

    private var _binding: FragmentClassifyBinding? = null
    private val binding get() = _binding!!

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

        val filterRv = binding.filterRv
        filterRv.setup {
            /**
             * 过滤列表
             */
            addType<FilterData>(R.layout.item_filter)

            onCreate {
                getBinding<ItemFilterBinding>().filterList.loadFilterList { item, index ->
                    val paramClass = ClassifyQueryParam::class.java
                    val fields = paramClass.declaredFields

                    val field = fields.first {
                        it.name == item.groupId
                    }

                    field.isAccessible = true
                    field.set(classifyQueryParam, if (index == 0 && field.name != "cateId") null else item.id)
                    binding.refreshLayout.showLoading()
                }
            }

            onBind {
                val model = getModel<FilterData>()
                getBinding<ItemFilterBinding>().apply {
                    filterName.text = model.name
                    filterList.models = model.items
                }
            }
        }

        binding.rv.loadImg().loadListCardMovies {
            val movieId = Repository.currentMovieId ?: return@loadListCardMovies
            VideoActivity.play(ParseParam(movieId = movieId, detailId = it.id, tvName = it.tvName))
        }

        binding.refreshLayout.onRefresh {
            viewLifecycleOwner.lifecycleScope.launch {
                classifyQueryParam.page = index
                MovieRepository.getClassify(classifyQueryParam).collect {
                    updateView(it)
                }
            }
        }.onError {
            showRouteDialog()
        }
    }

    private fun updateView(result: Result<CategoryData>) {
        val refreshLayout = binding.refreshLayout
        result.onSuccess { data ->
            // 如果是刷新操作
            val cateId = requireArguments().getString(ID, "1")
            if (classifyQueryParam.cateId ==  cateId && refreshLayout.index == 1 && data.filterData != null) {
                data.filterData.forEach {
                    it.items.getOrNull(0)?.isSelect = true
                    it.items.forEach { item -> item.groupId = it.id }
                }
                binding.filterRv.models = data.filterData
            }
            refreshLayout.addData(data.categoryData.result) {
                refreshLayout.index < data.categoryData.lastPage
            }
        }.onFailure {
            Log.e(TAG, it.toString())
            refreshLayout.showError(it.message)
        }
    }

    override fun isFirstResume() {
        super.isFirstResume()
        binding.refreshLayout.showLoading()
    }

    private fun RecyclerView.loadFilterList(block: (item: FilterData.Item, index: Int) -> Unit) {
        setup {
            addType<FilterData.Item>(R.layout.item_list_filter)
            R.id.tv_filter.onClick {
                val item = getModel<FilterData.Item>()
                mutable.forEachIndexed { index, i ->
                    i as FilterData.Item
                    if (i.isSelect && index != modelPosition) {
                        i.isSelect = false
                        notifyItemChanged(index)
                    } else if (index == modelPosition) {
                        i.isSelect = true
                        notifyItemChanged(index)
                    }
                }
                block(item, modelPosition)
            }
            onBind {
                val item = getModel<FilterData.Item>()
                getBinding<ItemListFilterBinding>().apply {
                    (itemView as TextView).apply {
                        text = item.name
                        setTypeface(null, if (item.isSelect) Typeface.BOLD else Typeface.NORMAL)
                    }
                    itemView.isSelected = item.isSelect
                }
            }
        }
    }

    private fun showRouteDialog() {
        MovieSourceDialog(requireActivity() as AppCompatActivity) {
            "正在刷新，请稍后".showToast()
            binding.refreshLayout.showLoading()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}