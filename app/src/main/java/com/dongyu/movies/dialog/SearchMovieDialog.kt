package com.dongyu.movies.dialog

import android.content.Context
import android.os.Bundle
import com.dongyu.movies.databinding.DialogSearchBinding
import com.dongyu.movies.fragment.search.SearchFragment
import com.google.android.material.bottomsheet.BottomSheetDialog

class SearchMovieDialog(context: Context, private val searchName: String?): BottomSheetDialog(context) {

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