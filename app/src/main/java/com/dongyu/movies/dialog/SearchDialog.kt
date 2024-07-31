package com.dongyu.movies.dialog

import android.app.Activity
import android.app.AppComponentFactory
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.dongyu.movies.R
import com.dongyu.movies.databinding.DialogSearchBinding
import com.dongyu.movies.fragment.search.SearchFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchDialog(context: Context, private val searchName: String?): BottomSheetDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DialogSearchBinding.inflate(layoutInflater)

        setContentView(binding.root)


        if (searchName == null) {
            return
        }

        val fragment = binding.searchFragment.getFragment<SearchFragment>()
        fragment.search(searchName)

    }

    override fun show() {
        super.show()
    }

}