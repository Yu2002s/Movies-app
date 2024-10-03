package com.dongyu.movies.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dongyu.movies.R
import com.dongyu.movies.activity.HistoryCollectActivity
import com.dongyu.movies.activity.VideoActivity
import com.dongyu.movies.databinding.FragmentHistoryBinding
import com.dongyu.movies.databinding.ItemListHistoryBinding
import com.dongyu.movies.model.movie.PlayHistory
import com.dongyu.movies.model.parser.ParseParam
import com.dongyu.movies.utils.getRelTime
import com.dongyu.movies.utils.getTime
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.litepal.LitePal
import org.litepal.extension.count
import org.litepal.extension.find
import kotlin.math.ceil

class HistoryCollectFragment : Fragment(), MenuItem.OnMenuItemClickListener {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val type get() = requireArguments().getInt(TYPE)

    companion object {
        private const val TYPE = "type"
        const val TYPE_HISTORY = 0
        const val TYPE_COLLECT = 1

        fun newInstance(type: Int): HistoryCollectFragment {
            val args = Bundle()
            args.putInt(TYPE, type)
            val fragment = HistoryCollectFragment()
            fragment.arguments = args
            return fragment
        }
    }

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

        val refreshLayout = binding.refreshLayout

        binding.rv.also { it.setHasFixedSize(true) }.setup {
            addType<PlayHistory>(R.layout.item_list_history)

            onBind {
                val playHistory = getModel<PlayHistory>()
                getBinding<ItemListHistoryBinding>().apply {
                    checkbox.isInvisible = !playHistory.isCheckable
                    checkbox.isChecked = playHistory.isChecked

                    historyName.text = playHistory.name
                    historyTime.text = playHistory.lastPlayTimeStr

                    historyPro.isInvisible = playHistory.progress >= 100
                    if (playHistory.progress >= 100) {
                        historyStartPro.text = itemView.context.getString(R.string.play_complete)
                    } else {
                        historyStartPro.text = playHistory.currentTime.getTime()
                        historyEndPro.text = playHistory.duration.getTime()
                        historyPro.progress = playHistory.progress
                    }

                    totalSelection.text = itemView.context.getString(R.string.total_selection)
                        .format(playHistory.totalSelection)
                    historyImg.setRadius(30)

                    Glide.with(this@HistoryCollectFragment)
                        .load(playHistory.cover)
                        .placeholder(R.drawable.image_loading)
                        .into(historyImg)
                }
            }

            onLongClick(R.id.history) {
                if (!toggleMode) {
                    toggle()
                    setChecked(layoutPosition, true)
                }
            }

            onFastClick(R.id.history, R.id.checkbox) {
                if (!toggleMode && it == R.id.history) {
                    val playHistory = getModel<PlayHistory>()
                    VideoActivity.play(
                        ParseParam(
                            movieId = playHistory.movieId,
                            detailId = playHistory.detailId,
                            tvName = playHistory.name
                        )
                    )
                    return@onFastClick
                }
                val checked = !getModel<PlayHistory>().isChecked
                setChecked(layoutPosition, checked)
            }

            onChecked { position, checked, allChecked ->
                val model = getModel<PlayHistory>(position)
                model.isChecked = checked
                notifyItemChanged(position)
            }

            onToggle { position, toggleMode, _ ->
                // 刷新列表显示选择按钮
                val model = getModel<PlayHistory>(position)
                model.isCheckable = toggleMode
                notifyItemChanged(position)
                changeListEditable(this)
            }
        }

        refreshLayout.onRefresh {
            lifecycleScope.launch(Dispatchers.IO) {
                /*val count = if (type == TYPE_HISTORY) {
                    LitePal.count<PlayHistory>()
                } else {
                    LitePal.where("isCollected = ?", type.toString()).count<PlayHistory>()
                }*/
                val count = LitePal.where("isCollected = ?", type.toString()).count<PlayHistory>()
                val endPage = ceil(count / 10f).toInt()

                val offset = (index - 1) * 10

                val query = LitePal.offset(offset).limit(10)
                    .where("isCollected = ?", type.toString())
                /*if (type == TYPE_COLLECT) {
                    query.where("isCollected = ?", type.toString())
                }*/
                val histories = query.order("lastPlayTime desc").find<PlayHistory>()

                histories.forEach {
                    it.lastPlayTimeStr = it.lastPlayTime.getRelTime()
                }

                requireActivity().runOnUiThread {
                    addData(histories) {
                        index <= endPage
                    }
                }
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val bindingAdapter = binding.rv.bindingAdapter
        when (item.itemId) {
            R.id.edit -> {
                bindingAdapter.toggle()
            }

            R.id.selectAll -> {
                bindingAdapter.checkedAll(true)
            }

            R.id.deselect -> {
                bindingAdapter.checkedAll(false)
            }

            R.id.delete -> {
                bindingAdapter.setDifferModels(bindingAdapter.models?.filter {
                    it as PlayHistory
                    if (it.isChecked) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            it.delete()
                        }
                    }
                    !it.isChecked
                })
                if (bindingAdapter.models!!.isEmpty()) {
                    binding.refreshLayout.showLoading()
                }
            }
        }
        return true
    }

    /** 改变编辑状态 */
    private fun changeListEditable(adapter: BindingAdapter) {
        val toggleMode = adapter.toggleMode
        val checkedCount = adapter.checkedCount

        (requireActivity() as HistoryCollectActivity).toggleMode = toggleMode

        // 如果取消管理模式则取消全部已选择
        if (!toggleMode) adapter.checkedAll(false)
    }

    override fun onResume() {
        super.onResume()
        binding.refreshLayout.refreshing()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}