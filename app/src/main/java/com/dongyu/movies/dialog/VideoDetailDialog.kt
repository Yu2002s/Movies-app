package com.dongyu.movies.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.dongyu.movies.data.movie.MovieItem
import com.dongyu.movies.databinding.DialogVideoDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class VideoDetailDialog : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(movieItem: MovieItem): VideoDetailDialog {
            val args = Bundle()
            args.putParcelable("movie", movieItem)
            val fragment = VideoDetailDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: DialogVideoDetailBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogVideoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BundleCompat.getParcelable(arguments ?: bundleOf(), "movie", MovieItem::class.java)
            ?.let { item ->
                binding.apply {
                    movieDesc.text = item.detail
                    movieInfo.apply {
                        movieName.text = item.tvName
                        movieStar.text = item.star
                        movieDirector.text = item.director
                        Glide.with(this@VideoDetailDialog)
                            .load(item.cover)
                            .into(movieCover)
                    }
                }
            }
    }
}