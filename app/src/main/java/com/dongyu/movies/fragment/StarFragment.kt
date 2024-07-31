package com.dongyu.movies.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dongyu.movies.adapter.MovieGridPagingAdapter
import com.dongyu.movies.databinding.FragmentStarBinding

class StarFragment: Fragment() {

    private var _binding: FragmentStarBinding? = null
    private val binding get() = _binding!!

    private val starAdapter = MovieGridPagingAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}