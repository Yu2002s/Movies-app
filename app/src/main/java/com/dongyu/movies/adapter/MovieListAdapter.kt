package com.dongyu.movies.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dongyu.movies.R
import com.dongyu.movies.data.movie.BaseMovieItem
import com.dongyu.movies.databinding.ItemListMovieBinding
import com.dongyu.movies.event.OnItemClickListener

class MovieListAdapter :
    PagingDataAdapter<BaseMovieItem, MovieListAdapter.MovieRecyclerViewHolder>(
        DIFF_CALLBACK
    ) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BaseMovieItem>() {
            override fun areContentsTheSame(
                oldItem: BaseMovieItem,
                newItem: BaseMovieItem
            ): Boolean {
                return oldItem.id == newItem.id //&& oldItem.tvName == newItem.tvName
            }

            override fun areItemsTheSame(oldItem: BaseMovieItem, newItem: BaseMovieItem): Boolean {
                return oldItem == newItem || oldItem.id == newItem.id // && oldItem.tvName == newItem.tvName
            }
        }
    }

    var onItemClickListener: OnItemClickListener? = null

    class MovieRecyclerViewHolder(
        binding: ItemListMovieBinding,
        onItemClickListener: OnItemClickListener?
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private val nameTv = binding.movieName
        private val cover = binding.movieCover
        private val star = binding.movieStar
        private val director = binding.movieDirector
        private val status = binding.status
        private val cate = binding.cate

        private val options = RequestOptions.bitmapTransform(RoundedCorners(16))
            .override(100, 225)

        init {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(it, absoluteAdapterPosition)
            }
        }

        fun bindTo(movieItem: BaseMovieItem) {
            nameTv.text = movieItem.tvName
            Glide.with(cover.context)
                .load(movieItem.cover)
                .placeholder(R.drawable.image_loading)
                .apply(options)
                .into(cover)
            status.text = movieItem.status
            cate.text = movieItem.cate
            star.text = star.context.getString(R.string.star).format(movieItem.star)
            director.text = director.context.getString(R.string.director).format(movieItem.director)
        }
    }

    override fun onBindViewHolder(holder: MovieRecyclerViewHolder, position: Int) {
        holder.bindTo(getItem(position)!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieRecyclerViewHolder {
        return MovieRecyclerViewHolder(
            ItemListMovieBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), onItemClickListener
        )
    }

}