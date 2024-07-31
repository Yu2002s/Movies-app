package com.dongyu.movies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.data.home.MoviesCard
import com.dongyu.movies.data.movie.BaseMovieItem
import com.dongyu.movies.data.movie.MovieItem
import com.dongyu.movies.databinding.ItemListCardBinding
import com.dongyu.movies.event.OnCardItemClickListener
import com.dongyu.movies.event.OnItemClickListener

class CardAdapter(private val cardList: List<MoviesCard<BaseMovieItem>>) :
    RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    var onCardItemClickListener: OnCardItemClickListener? = null

    class ViewHolder(
        val binding: ItemListCardBinding,
        onCardItemClickListener: OnCardItemClickListener?
    ) :
        RecyclerView.ViewHolder(binding.root) {

        val adapter = MoviesGridAdapter()

        init {
            adapter.onItemClickListener = object : OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    onCardItemClickListener?.onCardItemClick(
                        itemView,
                        view,
                        absoluteAdapterPosition,
                        position
                    )
                }
            }
            binding.cardRecyclerView.adapter = adapter
        }
    }

    override fun getItemCount() = cardList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), onCardItemClickListener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = cardList[position]
        holder.binding.apply {
            cardTitle.text = card.title
            holder.adapter.submitList(card.list)
        }
    }

}