package com.dongyu.movies.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dongyu.movies.R
import com.dongyu.movies.data.movie.BaseMovieItem
import com.dongyu.movies.databinding.ItemGridMoviesBinding
import com.dongyu.movies.event.OnItemClickListener

class MovieGridPagingAdapter : PagingDataAdapter<BaseMovieItem, MovieGridPagingAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    private val options = RequestOptions()
        .override(270, 380)
        .transform(CenterCrop(), RoundedCorners(16))

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BaseMovieItem>() {
            override fun areContentsTheSame(
                oldItem: BaseMovieItem,
                newItem: BaseMovieItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: BaseMovieItem, newItem: BaseMovieItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    var onItemClickListener: OnItemClickListener? = null

    class ViewHolder(val binding: ItemGridMoviesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemGridMoviesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).apply {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(it, absoluteAdapterPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movieItem = getItem(position) ?: return
        holder.binding.apply {
            Glide.with(movieImg)
                .load(movieItem.cover)
                .placeholder(R.drawable.image_loading)
                 .apply(options)
                .into(movieImg)
            movieName.text = movieItem.tvName
            movieStatus.text = movieItem.status
        }
    }
}