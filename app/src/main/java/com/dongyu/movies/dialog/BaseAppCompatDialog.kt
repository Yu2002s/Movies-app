package com.dongyu.movies.dialog

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

class BaseAppCompatDialog(context: Context): AppCompatDialog(context) {

    init {
        val windowHeight = getWindowHeight()
        // val windowWidth = getWindowWidth()
        setCanceledOnTouchOutside(true)
        window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        window?.attributes?.apply {
            gravity = Gravity.END
            width = 400
            height = windowHeight
            dimAmount = 0.1f
            windowAnimations = R.style.PopupWindowSlideAnim
        }
        window?.decorView?.layoutParams?.width = 400
        window?.decorView?.setBackgroundResource(R.drawable.bg_corner)
        setOnShowListener {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            WindowCompat.getInsetsController(window!!, window!!.decorView)
                .hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

}