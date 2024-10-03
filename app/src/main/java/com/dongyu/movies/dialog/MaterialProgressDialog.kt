package com.dongyu.movies.dialog

import android.app.Activity
import com.dongyu.movies.databinding.LayoutDialogProgressBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MaterialProgressDialog(activity: Activity): MaterialAlertDialogBuilder(activity) {

    val binding: LayoutDialogProgressBinding

    init {
        val layoutInflater = activity.layoutInflater
        binding = LayoutDialogProgressBinding.inflate(layoutInflater, null, false)
        setView(binding.root)
    }


    override fun setMessage(message: CharSequence?): MaterialAlertDialogBuilder {
        binding.tvMessage.text = message
        return this
    }
}