package com.dongyu.movies.base

import androidx.fragment.app.Fragment

open class BaseFragment: Fragment() {

    var isFirst = true

    protected open fun isFirstResume() {
        isFirst = false
    }

    override fun onResume() {
        super.onResume()
        if (isFirst) {
            isFirstResume()
        }
    }
}