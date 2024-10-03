package com.dongyu.movies.fragment.setting

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.dongyu.movies.R
import com.dongyu.movies.activity.MainActivity
import com.dongyu.movies.config.AppConfig
import com.dongyu.movies.config.AppConfig.QQ_GROUP_URL
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.model.movie.MovieResponse
import com.dongyu.movies.utils.ALiPayUtils
import com.dongyu.movies.utils.SpUtils.get
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.utils.toFileUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AboutFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        setPreferencesFromResource(R.xml.preference_about, p1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireActivity() is MainActivity) {
            val typedArray = requireContext()
                .obtainStyledAttributes(intArrayOf(com.google.android.material.R.attr.colorSurface))

            view.setBackgroundColor(typedArray.getColor(0, Color.WHITE))

            typedArray.recycle()
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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.GROUP_URL))
            startActivity(intent)
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

    private fun goQQGroup() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            QQ_GROUP_URL.toUri()
        )
        startActivity(intent)
    }

    private fun goPageHome() {
        startActivity(Intent(Intent.ACTION_VIEW, AppConfig.APP_PAGE_HOME.toUri()))
    }
}