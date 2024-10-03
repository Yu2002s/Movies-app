package com.dongyu.movies.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.dongyu.movies.R
import com.dongyu.movies.model.movie.MovieItem
import com.dongyu.movies.databinding.DialogVideoDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 影视详情对话框
 */
class MovieDetailDialog(context: Context, item: MovieItem) : BottomSheetDialog(context) {


   init {
       val inflater = LayoutInflater.from(context)
       val binding = DialogVideoDetailBinding.inflate(inflater, null, false)
       binding.apply {
           movieDesc.text = item.detail
           movieInfo.apply {
               status.text = item.status
               movieName.text = item.tvName
               movieStar.text = item.star
               movieDirector.text = item.director
               Glide.with(context)
                   .load(item.cover)
                   .placeholder(R.drawable.image_loading)
                   .into(movieCover)
           }
       }
       setContentView(binding.root)
   }
}