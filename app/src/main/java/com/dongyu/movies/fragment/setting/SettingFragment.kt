package com.dongyu.movies.fragment.setting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bumptech.glide.Glide
import com.cat.sdk.utils.QSpUtils
import com.dongyu.movies.R
import com.dongyu.movies.activity.LoginActivity
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.network.Repository
import com.dongyu.movies.model.user.User
import com.dongyu.movies.dialog.MovieSourceDialog
import com.dongyu.movies.utils.ThemeUtils
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.viewmodel.UserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

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
            if (Repository.isLogin()) {
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

        findPreference<Preference>("clear_cache")?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle("提示")
                setMessage("是否清理缓存？")
                setNegativeButton("取消", null)
                setPositiveButton("确认") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        Glide.get(requireContext()).clearDiskCache()
                        val ymd = getDateNoLineToString(System.currentTimeMillis() / 1000)
                        QSpUtils.removeToSP(requireContext(), "ruler$ymd");
                        requireActivity().runOnUiThread {
                            "缓存已清理".showToast()
                        }
                    }
                }
                show()
            }
            true
        }

        findPreference<Preference>("route_id")?.setOnPreferenceClickListener {
            MovieSourceDialog(requireActivity() as AppCompatActivity) {
                "请返回主页刷新".showToast()
            }
            true
        }

        findPreference<DropDownPreference>(SPConfig.APP_THEME)?.setOnPreferenceChangeListener { _, any ->
            ThemeUtils.setTheme(requireActivity(), any.toString())
            ThemeUtils.notifyThemeChanged()
            true
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateNoLineToString(time: Long): String {
        val d = Date(time * 1000)
        val sf = SimpleDateFormat("yyyyMMdd")
        return sf.format(d)
    }

    private fun goLogin() {
        launcher.launch(Intent(requireContext(), LoginActivity::class.java))
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