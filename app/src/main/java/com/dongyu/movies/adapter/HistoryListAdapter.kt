package com.dongyu.movies.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dongyu.movies.R
import com.dongyu.movies.data.history.PlayHistory
import com.dongyu.movies.databinding.ItemListHistoryBinding
import com.dongyu.movies.event.OnItemClickListener
import com.dongyu.movies.utils.getTime

class HistoryListAdapter : PagingDataAdapter<PlayHistory, HistoryListAdapter.HistoryViewHolder>(
    DIFF_CALLBACK
) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PlayHistory>() {
            override fun areContentsTheSame(oldItem: PlayHistory, newItem: PlayHistory): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: PlayHistory, newItem: PlayHistory): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    var onItemClickListener: OnItemClickListener? = null

    var onItemLongClickListener: OnItemClickListener? = null

    class HistoryViewHolder(binding: ItemListHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val cover = binding.historyImg
        private val name = binding.historyName
        private val time = binding.historyTime
        private val startPro = binding.historyStartPro
        private val endPro = binding.historyEndPro
        private val pro = binding.historyPro
        private val proGroup = binding.progressGroup
        private val totalSelection = binding.totalSelection

        private val roundedCorner = RequestOptions.bitmapTransform(RoundedCorners(16))
            .override(300, 500)

        fun bindTo(playHistory: PlayHistory?) {
            if (playHistory == null) {
                return
            }
            name.text = playHistory.name
            time.text = playHistory.timeStr

            proGroup.isInvisible = playHistory.progress >= 100
            if (playHistory.progress >= 100) {
                startPro.text = itemView.context.getString(R.string.play_complete)
            } else {
                startPro.text = playHistory.current.getTime()
                endPro.text = playHistory.duration.getTime()
                pro.progress = playHistory.progress
            }

            totalSelection.text = itemView.context.getString(R.string.total_selection)
                .format(playHistory.totalSelection)

            Glide.with(cover.context)
                .load(playHistory.cover)
                .placeholder(R.drawable.image_loading)
                .apply(roundedCorner)
                .into(cover)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            ItemListHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).apply {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(it, absoluteAdapterPosition)
            }
            itemView.setOnLongClickListener {
                onItemLongClickListener?.onItemClick(it, absoluteAdapterPosition)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }
}