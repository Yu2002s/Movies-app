package com.dongyu.movies.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.data.home.RankItem
import com.dongyu.movies.databinding.ItemListRankBinding
import com.dongyu.movies.event.OnItemClickListener

class RankListAdapter :
    RecyclerView.Adapter<RankListAdapter.ViewHolder>() {

    private val rankListItems = mutableListOf<RankItem.RankListItem>()

    var onItemClickListener: OnItemClickListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<RankItem.RankListItem>) {
        rankListItems.clear()
        rankListItems.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemListRankBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListRankBinding.inflate(
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

    override fun getItemCount() = rankListItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rankListItem = rankListItems[position]
        holder.binding.apply {
            rankNum.text = (position + 1).toString()
            tvName.text = rankListItem.name
        }
    }


}