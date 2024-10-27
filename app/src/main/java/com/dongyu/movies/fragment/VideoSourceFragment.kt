package com.dongyu.movies.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.R
import com.dongyu.movies.activity.VideoSourceActivity
import com.dongyu.movies.databinding.ItemListSelectionBinding
import com.dongyu.movies.dialog.MaterialProgressDialog
import com.dongyu.movies.model.movie.VideoSource
import com.dongyu.movies.network.MovieRepository
import com.dongyu.movies.utils.showToast
import com.drake.brv.utils.setup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.ArrayList

class VideoSourceFragment : Fragment() {

    companion object {

        private const val PARAM_ITEMS = "items"

        private const val PARAM_NAME = "name"

        fun newInstance(items: List<VideoSource.Item>): VideoSourceFragment {
            val args = Bundle()
            args.putParcelableArrayList(PARAM_ITEMS, items as ArrayList<out Parcelable>)
            val fragment = VideoSourceFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return RecyclerView(requireContext()).also {
            it.layoutManager = GridLayoutManager(requireContext(), 3)
            it.layoutParams = ViewGroup.LayoutParams(-1, -1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view as RecyclerView
        rv.setup {
            addType<VideoSource.Item>(R.layout.item_list_selection)

            onBind {
                getBinding<ItemListSelectionBinding>().selectionName.text =
                    getModel<VideoSource.Item>().name
            }

            R.id.item.onClick {
                // 开始下载
                getDownloadUrl(getModel())
            }
        }.models = requireArguments().getParcelableArrayList<VideoSource.Item>(PARAM_ITEMS)
    }

    private fun getDownloadUrl(item: VideoSource.Item) {
        val dialog = MaterialProgressDialog(requireActivity()).setTitle("正在获取下载地址...").show()
        lifecycleScope.launch {
            MovieRepository.getMovieVideo(item.param).collect { result ->
               dialog.dismiss()
                result.onSuccess { video ->
                    showDownloadDialog(video.url, item)
                }.onFailure {
                    "获取视频地址失败".showToast()
                }
            }
        }
    }

    private fun showDownloadDialog(url: String, item: VideoSource.Item) {
        val groupName = requireActivity().intent.getStringExtra(PARAM_NAME) ?: ""
        val name = groupName + " " + item.name
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("确认下载")
            setMessage("您确认要下载 $name")
            setPositiveButton("下载") { _, _ ->
                // 获取到了视频地址，开始下载
                (requireActivity() as VideoSourceActivity).addDownload(url, name, groupName)
            }
            setNegativeButton("取消", null)
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}