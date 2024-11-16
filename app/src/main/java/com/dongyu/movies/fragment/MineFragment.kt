package com.dongyu.movies.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dongyu.movies.R
import com.dongyu.movies.activity.DownloadActivity
import com.dongyu.movies.activity.HistoryCollectActivity
import com.dongyu.movies.activity.SettingActivity
import com.dongyu.movies.config.AppConfig
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.databinding.FragmentMineBinding
import com.dongyu.movies.databinding.ItemGridActionBinding
import com.dongyu.movies.model.ActionItem
import com.dongyu.movies.network.AppRepository
import com.dongyu.movies.utils.SpUtils.get
import com.dongyu.movies.utils.SpUtils.put
import com.drake.brv.utils.setup
import com.wanban.screencast.ScreenCastUtils
import com.wanban.screencast.listener.OnSearchedDevicesListener
import com.wanban.screencast.model.DeviceModel
import kotlinx.coroutines.launch

class MineFragment : Fragment() {

    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!

    companion object {

        private const val BING_HOST = "https://cn.bing.com"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SPConfig.MINE_COVER.get<String?>(null)?.let {
            loadCover(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            AppRepository.getBingImage().collect { result ->
                result.onSuccess {
                    val url = BING_HOST + it.images.getOrNull(0)?.url
                    SPConfig.MINE_COVER put url
                    loadCover(url)
                }
            }
        }

        binding.actionRv.setup {
            addType<ActionItem>(R.layout.item_grid_action)
            onBind {
                val actionItem = getModel<ActionItem>()
                getBinding<ItemGridActionBinding>().root.apply {
                    text = actionItem.title
                    val icon = ContextCompat.getDrawable(requireContext(), actionItem.icon)
                    setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)
                }
            }
            R.id.action_item.onClick {
                when (modelPosition) {
                    0 -> com.dongyu.movies.utils.startActivity<DownloadActivity>()
                    1 -> com.dongyu.movies.utils.startActivity<HistoryCollectActivity>()
                    2 -> com.dongyu.movies.utils.startActivity<SettingActivity>()
                    3 -> startActivity(Intent(Intent.ACTION_VIEW, AppConfig.MP_URL.toUri()))
                    4 -> startActivity(Intent(Intent.ACTION_VIEW, AppConfig.APP_HELP_URL.toUri()))
                }
            }
        }.models = getActionList()
    }

    private fun loadCover(url: String) {
        Glide.with(this@MineFragment)
            .load(url)
            .placeholder(R.drawable.image_loading)
            .into(binding.cover)
    }

    private fun getActionList() = listOf(
        ActionItem(R.drawable.baseline_arrow_circle_down_24, "下载管理"),
        ActionItem(R.drawable.baseline_history_24, "历史收藏"),
        ActionItem(R.drawable.baseline_settings_24, getString(R.string.nav_setting)),
        ActionItem(R.drawable.baseline_group_24, "反馈/交流"),
        ActionItem(R.drawable.baseline_help_outline_24, "使用帮助")
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}