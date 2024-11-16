package com.dongyu.movies.dialog

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.dongyu.movies.R
import com.dongyu.movies.utils.getWindowHeight
import com.dongyu.movies.utils.getWindowWidth
import com.kwad.sdk.contentalliance.coupon.model.ActivityInfo

class BaseAppCompatDialog(context: Context): AppCompatDialog(context) {

    init {
        setCanceledOnTouchOutside(true)
        window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        window?.decorView?.setBackgroundResource(R.drawable.bg_corner)
        setOnShowListener {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            WindowCompat.getInsetsController(window!!, window!!.decorView)
                .hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    override fun onStart() {
        super.onStart()
        val wh = getWindowHeight()
        val ww = getWindowWidth()
        val isProfit = wh > ww
        window?.attributes?.apply {
            gravity = Gravity.END
            width = if (isProfit) ww else wh - 200
            height = if (isProfit) WindowManager.LayoutParams.WRAP_CONTENT else wh
            dimAmount = 0.1f
            windowAnimations = R.style.PopupWindowSlideAnim
        }
    }

}