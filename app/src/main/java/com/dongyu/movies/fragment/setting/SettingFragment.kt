package com.dongyu.movies.fragment.setting

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.dongyu.movies.MoviesApplication
import com.dongyu.movies.MoviesApplication.Companion.QQ_GROUP_URL
import com.dongyu.movies.R
import com.dongyu.movies.activity.LoginActivity
import com.dongyu.movies.base.BaseRepository
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.data.movie.MovieResponse
import com.dongyu.movies.data.user.User
import com.dongyu.movies.dialog.RouteDialog
import com.dongyu.movies.utils.ALiPayUtils
import com.dongyu.movies.utils.SpUtils.get
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.utils.toFileUri
import com.dongyu.movies.viewmodel.UserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SettingFragment : PreferenceFragmentCompat() {

    companion object {
        private val TAG = this::class.java.simpleName
    }

    private val userViewModel by viewModels<UserViewModel>()

    private lateinit var launcher: ActivityResultLauncher<Intent>

    private val userPreference by lazy {
        findPreference<Preference>("userinfo")
    }

    private val logoutPreference by lazy {
        findPreference<Preference>("logout")
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_setting, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                userPreference?.apply {
                    title = "加载中..."
                    summary = "正在加载用户信息..."
                }
                // 登录成功了，刷新用户信息
                userViewModel.refreshUser()
            }
        }

        userPreference?.setOnPreferenceClickListener {
            if (BaseRepository.isLogin()) {
                // 已登录了
                return@setOnPreferenceClickListener false
            }
            goLogin()
            false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.userUiState.collect { user ->
                updateUserInfo(user)
            }
        }

        logoutPreference?.setOnPreferenceClickListener {
            userViewModel.logout()
            goLogin()
            false
        }

        findPreference<Preference>("statement")?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle("免责声明")
                setMessage(getString(R.string.statement))
                setPositiveButton("关闭", null)
                show()
            }
            true
        }

        findPreference<Preference>("contact")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/dongyu2002"))
            startActivity(intent)
            true
        }

        findPreference<Preference>("group")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(MoviesApplication.GROUP_URL))
            startActivity(intent)
            true
        }

        findPreference<Preference>("route_id")?.setOnPreferenceClickListener {
            RouteDialog(requireActivity() as AppCompatActivity) {
                "请返回主页刷新".showToast()
            }
            true
        }

        findPreference<Preference>("donation")?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle("捐赠")
                setMessage("你的捐赠将是我开发最大的动力！")
                setPositiveButton("支付宝") { _, _ ->
                    ALiPayUtils.startAlipayClient()
                }
                // setNegativeButton("微信") { _, _ -> }
                show()
            }
            true
        }

        findPreference<Preference>("qq_group")?.setOnPreferenceClickListener {
            goQQGroup()
            true
        }

        findPreference<Preference>("home_page")?.setOnPreferenceClickListener {
            goPageHome()
            true
        }

        findPreference<Preference>("share_app")?.setOnPreferenceClickListener {
            val appFile = requireContext().packageManager.getApplicationInfo(
                requireContext().packageName,
                0
            ).sourceDir
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk")
            intent.putExtra(Intent.EXTRA_STREAM, appFile.toFileUri())
            startActivity(Intent.createChooser(intent, "分享App到..."))
            true
        }
    }

    private fun showMoviesList(result: Result<List<MovieResponse.Movie>>) {
        result.onSuccess { list ->
            val currentMovieId = SPConfig.CURRENT_ROUTE_ID.get<Int?>(-1)
            val index = list.indexOfFirst {
                it.id == currentMovieId
            }
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle("线路列表")
                setSingleChoiceItems(list.map { it.name }.toTypedArray(), index, null)
                show()
            }
        }.onFailure {
            it.message.showToast()
        }
    }

    private fun goLogin() {
        launcher.launch(Intent(requireContext(), LoginActivity::class.java))
    }

    private fun goQQGroup() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            QQ_GROUP_URL.toUri()
        )
        startActivity(intent)
    }

    private fun goPageHome() {
        startActivity(Intent(Intent.ACTION_VIEW, MoviesApplication.APP_PAGE_HOME.toUri()))
    }

    /**
     * 更新用户基本信息
     */
    private fun updateUserInfo(user: User?) {
        logoutPreference?.isVisible = user != null
        userPreference?.apply {
            if (user != null) {
                title = user.nickname
                summary = user.email
                // avatar 待完成
            } else {
                title = getString(R.string.please_login)
                summary = getString(R.string.click_login)
                icon = null
            }
        }
    }
}